package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.service.IErpSipmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ErpSipmServiceImpl implements IErpSipmService {

    private static final String BASE_URL = "http://192.168.9.28:18088";
    private static final String AUTH_URL = BASE_URL + "/sipmweb/api/oauth";
    private static final String DOWNLOAD_PATH = BASE_URL + "/sipmweb/web/download";
    private static final String SEARCH_PATH = BASE_URL + "/sipmweb/api/{rid}/search/{table}/filter";

    private static final String TABLE_PART = "MPART";
    private static final String TABLE_DRAWING = "DWGPE";

    private static final String USERNAME = "adm";
//    private static final String USERNAME = "apiuser";
    private static final String PASSWORD = "31966f29c4f5041baf86934211c6381f";
//    private static final String PASSWORD = "50f116efe0a1c606cddc174216afaa3a";

    private static final String ERR_CODE_SUCCESS = "0";
    private static final String ERR_CODE_NO_DATA = "30002";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private volatile String cachedToken="692131c1d76430c1189506c5081700cf";

    public ErpSipmServiceImpl(@Qualifier("erpRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String auth() {
        if (StrUtil.isNotBlank(cachedToken)) {
            return cachedToken;
        }
        synchronized (this) {
            if (StrUtil.isBlank(cachedToken)) {
                cachedToken = requestToken();
            }
            return cachedToken;
        }
    }

    private String refreshToken() {
        synchronized (this) {
            cachedToken = requestToken();
            return cachedToken;
        }
    }

    private String requestToken() {
        URI uri = UriComponentsBuilder.fromUriString(AUTH_URL)
                .queryParam("uname", USERNAME)
                .queryParam("passwd", PASSWORD)
                .queryParam("f", false)
                .build()
                .toUri();
        String responseBody = restTemplate.getForObject(uri, String.class);
        String token = parseToken(responseBody);
        if (StrUtil.isBlank(token)) {
            log.error("SIPM认证未获取到token，响应体:{}", responseBody);
            throw new IllegalStateException("SIPM认证失败");
        }
        return token;
    }

    @Override
    public Object searchPartList(String no, Integer start, Integer size) {
        if (StrUtil.isBlank(no)) {
            throw new IllegalArgumentException("物料编码不能为空");
        }
        String partFilter = buildNoFilter(no.trim());
        String token = auth();
        Object partResult = searchByToken(token, TABLE_PART, partFilter, start, size, true);
        if (isRequestFailed(partResult)) {
            log.warn("SIPM物料查询失败，清理缓存token后重试一次，响应:{}", partResult);
            cachedToken = null;
            token = refreshToken();
            partResult = searchByToken(token, TABLE_PART, partFilter, start, size, true);
        }
        if (isRequestFailed(partResult)) {
            return partResult;
        }

        List<Object> partList = extractList(partResult);
        if (partList.isEmpty()) {
            return buildSplitResult(null, List.of());
        }

        Object firstPart = partList.get(0);
        List<Object> drawingList = new ArrayList<>();
        for (Object partItem : partList) {
            if (!(partItem instanceof Map<?, ?> partRow)) {
                continue;
            }
            String photoNo = extractPhotoNo(partRow);
            if (StrUtil.isBlank(photoNo)) {
                continue;
            }
            Object drawingResult = searchByToken(token, TABLE_DRAWING, buildNoFilter(photoNo), start, size, false);
            if (isRequestFailed(drawingResult)) {
                log.warn("SIPM图纸查询失败，清理缓存token后重试一次，photoNo:{}，响应:{}", photoNo, drawingResult);
                cachedToken = null;
                token = refreshToken();
                drawingResult = searchByToken(token, TABLE_DRAWING, buildNoFilter(photoNo), start, size, false);
            }
            if (isRequestFailed(drawingResult)) {
                return drawingResult;
            }
            drawingList.addAll(extractList(drawingResult));
        }
        return buildSplitResult(firstPart, drawingList);
    }

    private Object searchByToken(String token, String table, String filter, Integer start, Integer size, boolean includeExtra) {
        URI uri = UriComponentsBuilder.fromUriString(SEARCH_PATH)
                .queryParam("sort", 1)
                .queryParam("start", start == null ? 0 : start)
                .queryParam("size", size == null ? -1 : size)
                .queryParam("filter", filter)
                .queryParamIfPresent("extra", includeExtra ? java.util.Optional.of("TH") : java.util.Optional.empty())
                .buildAndExpand(token, table)
                .encode()
                .toUri();
        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            return parseJsonIfPossible(responseBody);
        } catch (RestClientException e) {
            log.warn("SIPM列表查询请求异常，table:{}，filter:{}", table, filter, e);
            return Map.of("errcode", -1, "errmsg", StrUtil.blankToDefault(e.getMessage(), "SIPM列表查询请求异常"));
        }
    }

    private String buildNoFilter(String no) {
        return "DEL=0 AND WKAID <>'3' AND NO='" + no.replace("'", "''") + "'";
    }

    private String extractPhotoNo(Map<?, ?> partRow) {
        if (partRow.get("extra") instanceof Map<?, ?> extra) {
            Object th = extra.get("TH");
            return th == null ? null : String.valueOf(th).trim();
        }
        return null;
    }

    private List<Object> extractList(Object result) {
        if (result instanceof Map<?, ?> map && map.get("list") instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return List.of();
    }

    private Map<String, Object> buildListResult(List<Object> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("errcode", 0);
        result.put("errmsg", null);
        result.put("total", list.size());
        return result;
    }

    private Map<String, Object> buildSplitResult(Object part, List<Object> drawings) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("part", part);
        result.put("drawings", drawings);
        result.put("errcode", 0);
        result.put("errmsg", null);
        result.put("total", drawings.size());
        return result;
    }

    @Override
    public ResponseEntity<byte[]> getImageUrl(String id, String type) {
        if (StrUtil.isBlank(id)) {
            throw new IllegalArgumentException("图片ID不能为空");
        }
        String token = auth();
        ResponseEntity<byte[]> response = downloadByToken(token, id.trim());
        if (isDownloadTokenFailed(response)) {
            log.warn("SIPM附件下载疑似token失效，清理缓存token后重试一次，id:{}", id);
            cachedToken = null;
            token = refreshToken();
            response = downloadByToken(token, id.trim());
        }
//        saveDownloadFile(response, id.trim());
        return buildDownloadResponse(response);
    }

    private ResponseEntity<byte[]> downloadByToken(String token, String id) {
        URI uri = UriComponentsBuilder.fromUriString(DOWNLOAD_PATH)
                .queryParam("rid", token)
                .queryParam("id", id)
                .queryParam("t", "DWGPE")
                .queryParam("type", "BD")
                .build()
                .encode()
                .toUri();
        try {
            return restTemplate.getForEntity(uri, byte[].class);
        } catch (RestClientResponseException e) {
            log.warn("SIPM附件下载响应异常，id:{}，status:{}", id, e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders() == null ? new HttpHeaders() : e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (RestClientException e) {
            log.warn("SIPM附件下载请求异常，id:{}", id, e);
            throw e;
        }
    }

    private ResponseEntity<byte[]> buildDownloadResponse(ResponseEntity<byte[]> response) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = response.getHeaders().getContentType();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType);
        headers.setContentLength(response.getBody() == null ? 0 : response.getBody().length);
        List<String> contentDisposition = response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION);
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            headers.put(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        }
        return ResponseEntity.status(response.getStatusCode())
                .headers(headers)
                .body(response.getBody());
    }

    private boolean isDownloadTokenFailed(ResponseEntity<byte[]> response) {
        if (response.getStatusCode().value() == 401 || response.getStatusCode().value() == 403) {
            return true;
        }
        byte[] body = response.getBody();
        if (body == null || body.length == 0) {
            return !response.getStatusCode().is2xxSuccessful();
        }
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && MediaType.APPLICATION_JSON.includes(contentType)) {
            Object parsed = parseJsonIfPossible(new String(body, java.nio.charset.StandardCharsets.UTF_8));
            return isRequestFailed(parsed);
        }
        String bodyText = new String(body, java.nio.charset.StandardCharsets.UTF_8).trim();
        if (bodyText.startsWith("{") || bodyText.startsWith("[")) {
            return isRequestFailed(parseJsonIfPossible(bodyText));
        }
        return false;
    }

    private void saveDownloadFile(ResponseEntity<byte[]> response, String id) {
        byte[] body = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || body == null || body.length == 0) {
            log.warn("SIPM附件下载未保存，id:{}，status:{}，bodySize:{}", id, response.getStatusCode(), body == null ? 0 : body.length);
            return;
        }
        try {
            Path directory = Paths.get(System.getProperty("user.dir"), "target", "sipm-download");
            Files.createDirectories(directory);
            String fileName = System.currentTimeMillis() + "_" + sanitizeFileName(id) + resolveDownloadExtension(response);
            Path filePath = directory.resolve(fileName);
            Files.write(filePath, body);
            log.info("SIPM附件下载文件已保存，id:{}，path:{}，size:{}", id, filePath.toAbsolutePath(), body.length);
        } catch (IOException e) {
            log.warn("SIPM附件下载文件保存失败，id:{}", id, e);
        }
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String resolveDownloadExtension(ResponseEntity<byte[]> response) {
        byte[] body = response.getBody();
        MediaType contentType = response.getHeaders().getContentType();
        if ((contentType != null && MediaType.APPLICATION_PDF.includes(contentType)) || startsWith(body, "%PDF-".getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return ".pdf";
        }
        if (contentType != null) {
            if (MediaType.IMAGE_PNG.includes(contentType)) {
                return ".png";
            }
            if (MediaType.IMAGE_JPEG.includes(contentType)) {
                return ".jpg";
            }
            if (MediaType.IMAGE_GIF.includes(contentType)) {
                return ".gif";
            }
        }
        return ".bin";
    }

    private boolean startsWith(byte[] body, byte[] prefix) {
        if (body == null || body.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (body[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String parseToken(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return null;
        }
        String trimmedBody = responseBody.trim();
        if (!trimmedBody.startsWith("{") && !trimmedBody.startsWith("[")) {
            return trimmedBody;
        }
        try {
            Map<String, Object> body = objectMapper.readValue(trimmedBody, new TypeReference<>() {});
            Object errcode = body.get("errcode");
            if (errcode != null && !"0".equals(String.valueOf(errcode))) {
                log.error("SIPM认证失败，响应体:{}", responseBody);
                return null;
            }
            for (String key : tokenKeys()) {
                Object value = body.get(key);
                if (value != null && StrUtil.isNotBlank(String.valueOf(value))) {
                    return String.valueOf(value);
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("SIPM认证响应不是可解析JSON，按原始文本处理");
        }
        return null;
    }

    private List<String> tokenKeys() {
        return List.of("rid", "token", "access_token", "sessionId", "sessionid", "id", "errmsg");
    }

    private boolean isRequestFailed(Object result) {
        if (result instanceof Map<?, ?> map) {
            Object errcode = map.get("errcode");
            if (errcode == null) {
                return false;
            }
            String errcodeText = String.valueOf(errcode);
            return !ERR_CODE_SUCCESS.equals(errcodeText) && !ERR_CODE_NO_DATA.equals(errcodeText);
        }
        return false;
    }

    private Object parseJsonIfPossible(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return null;
        }
        String trimmedBody = responseBody.trim();
        if (!trimmedBody.startsWith("{") && !trimmedBody.startsWith("[")) {
            return trimmedBody;
        }
        try {
            return objectMapper.readValue(trimmedBody, Object.class);
        } catch (JsonProcessingException e) {
            log.warn("SIPM列表响应JSON解析失败，返回原始文本", e);
            return responseBody;
        }
    }
}
