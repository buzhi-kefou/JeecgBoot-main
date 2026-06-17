package org.jeecg.modules.erp.service.impl;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.config.ErpConfigProperties;
import org.jeecg.modules.erp.service.IErpAuthService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class ErpAuthServiceImpl implements IErpAuthService {

    @Resource
    private ErpConfigProperties erpConfigProperties;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public String login() {
        Map<String, Object> body = new HashMap<>();
        body.put("lcid", erpConfigProperties.getLcid());
        body.put("appId", erpConfigProperties.getAppId());
        body.put("acctID", erpConfigProperties.getAcctID());
        body.put("username", erpConfigProperties.getUsername());
        body.put("appSecret", erpConfigProperties.getAppSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                erpConfigProperties.getAuthUrl(),
                requestEntity,
                String.class);
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        String sessionToken = null;

        if (setCookies != null) {
            for (String setCookie : setCookies) {
                List<HttpCookie> cookies = HttpCookie.parse(setCookie);

                for (HttpCookie cookie : cookies) {
                    if (erpConfigProperties.getHeaderKey().equalsIgnoreCase(cookie.getName())) {
                        sessionToken = cookie.getValue();
                    }
                }
            }
        }

        if (StrUtil.isBlank(sessionToken)) {
            log.error("ERP登录未获取到会话Cookie，响应体:{}", response.getBody());
            throw new IllegalStateException("金蝶云登录失败");
        }

        erpConfigProperties.setHeaderToken(sessionToken);
        log.info("ERP登录成功，会话token:{}", sessionToken);
        return sessionToken;
    }
}
