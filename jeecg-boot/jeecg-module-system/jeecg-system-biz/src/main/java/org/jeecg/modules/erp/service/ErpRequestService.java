package org.jeecg.modules.erp.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.erp.config.ErpConfigProperties;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpCommonEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ErpRequestService {

    @Resource
    private ErpConfigProperties erpConfigProperties;

    @Resource
    private IErpAuthService erpAuthService;

    @Resource
    private RestTemplate restTemplate;

    public <T> List<T> request(QueryDto dto, Class<T> clazz) {
        QueryDetailDto detailDto = getQueryDetailDto(dto);
        Integer limit = detailDto.getLimit();
        List<T> result = new ArrayList<>();

        String token = erpAuthService.login();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, erpConfigProperties.getHeaderKey() + "=" + token);

        while (true) {
            String jsonString = JSONObject.toJSONString(dto);
            log.error("请求体参数：{}", jsonString);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonString, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    erpConfigProperties.getQueryUrl(),
                    requestEntity,
                    String.class);

            String respBody = response.getBody();
            List<T> rows = parseRows(respBody, detailDto.getFieldKeys(), clazz);
            result.addAll(rows);

            if (limit == null || limit <= 0 || rows.size() != limit) {
                break;
            }
            Integer startRow = detailDto.getStartRow();
            detailDto.setStartRow((startRow == null ? 0 : startRow) + limit);
        }

        return result;
    }

    private static QueryDetailDto getQueryDetailDto(QueryDto dto) {
        if (dto == null || dto.getParameters() == null || dto.getParameters().isEmpty()) {
            throw new IllegalArgumentException("ERP查询参数不能为空");
        }
        QueryDetailDto detailDto = dto.getParameters().get(0);
        if (detailDto == null) {
            throw new IllegalArgumentException("ERP查询明细参数不能为空");
        }
        return detailDto;
    }

    public static <T> List<T> parseRows(String json, String parameters, Class<T> clazz) {
        List<String> fields = Arrays.asList(parameters.split(","));
        Map<String, Field> fieldMap = getJsonPropertyFieldMap(clazz);
        List<NestedFieldMapping> nestedFieldMappings = getNestedFieldMappings(clazz);
        JSONArray rows = JSON.parseArray(json);
        handleErpErrorResponse(rows);

        List<T> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            JSONArray row = rows.getJSONArray(i);

            JSONObject obj = new JSONObject();
            Map<NestedFieldMapping, JSONObject> nestedObjects = new HashMap<>();
            for (int j = 0; j < fields.size() && j < row.size(); j++) {
                String fieldName = fields.get(j);
                Object value = row.get(j);
                Field targetField = fieldMap.get(fieldName);
                if (targetField != null) {
                    obj.put(fieldName, normalizeErpValue(value, targetField));
                }
                for (NestedFieldMapping nestedFieldMapping : nestedFieldMappings) {
                    Field nestedTargetField = nestedFieldMapping.fieldMap().get(fieldName);
                    if (nestedTargetField != null) {
                        nestedObjects.computeIfAbsent(nestedFieldMapping, key -> new JSONObject())
                                .put(fieldName, normalizeErpValue(value, nestedTargetField));
                    }
                }
            }

            T entity;
            try {
                entity = obj.toJavaObject(clazz);
            } catch (NumberFormatException e) {
                // 定位具体哪个字段转换失败
                String errorMsg = "数字格式错误，详细字段信息：";
                for (int j = 0; j < fields.size() && j < row.size(); j++) {
                    String fieldName = fields.get(j);
                    Object value = row.get(j);
                    Field targetField = fieldMap.get(fieldName);
                    if (targetField != null) {
                        try {
                            normalizeErpValue(value, targetField);
                        } catch (NumberFormatException ex) {
                            errorMsg += "\n字段名: " + fieldName + " (JSON属性: " + targetField.getName() + "), 值: '" + value + "', 类型: " + targetField.getType().getSimpleName();
                        }
                    }
                }
                throw new NumberFormatException(errorMsg + "\n原始错误: " + e.getMessage());
            }
            setNestedObjects(entity, nestedObjects);
            result.add(entity);
        }

        return result;
    }

    private static void handleErpErrorResponse(JSONArray rows) {
        List<String> messages = new ArrayList<>();
        collectErpErrorMessages(rows, messages);
        if (messages.isEmpty()) {
            return;
        }
        String message = messages.stream().filter(StrUtil::isNotBlank)
                .distinct().collect(Collectors.joining("；"));

        log.error("ERP请求失败：{}", message);
        throw new IllegalStateException("ERP请求失败：" + message);
    }

    private static void collectErpErrorMessages(Object value, List<String> messages) {
        if (value instanceof JSONArray array) {
            for (Object item : array) {
                collectErpErrorMessages(item, messages);
            }
            return;
        }
        if (!(value instanceof JSONObject object)) {
            return;
        }

        JSONObject responseStatus = null;
        JSONObject result = object.getJSONObject("Result");
        if (result != null) {
            responseStatus = result.getJSONObject("ResponseStatus");
        } else if (object.containsKey("ResponseStatus")) {
            responseStatus = object.getJSONObject("ResponseStatus");
        }

        if (responseStatus == null || responseStatus.getBooleanValue("IsSuccess")) {
            for (Object item : object.values()) {
                collectErpErrorMessages(item, messages);
            }
            return;
        }

        JSONArray errors = responseStatus.getJSONArray("Errors");
        if (errors == null || errors.isEmpty()) {
            String msgCode = responseStatus.getString("MsgCode");
            if (StrUtil.isNotBlank(msgCode)) {
                messages.add("MsgCode=" + msgCode);
            }
            return;
        }
        for (int i = 0; i < errors.size(); i++) {
            JSONObject error = errors.getJSONObject(i);
            if (error == null) {
                continue;
            }
            String message = error.getString("Message");
            if (StrUtil.isNotBlank(message)) {
                messages.add(message);
            }
        }
    }

    private static Map<String, Field> getJsonPropertyFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null && StrUtil.isNotBlank(jsonProperty.value())) {
                    fieldMap.put(jsonProperty.value(), field);
                }
            }
            current = current.getSuperclass();
        }
        return fieldMap;
    }

    private static List<NestedFieldMapping> getNestedFieldMappings(Class<?> clazz) {
        List<NestedFieldMapping> mappings = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Class<?> nestedEntityType = resolveNestedEntityType(field);
                if (nestedEntityType != null) {
                    mappings.add(new NestedFieldMapping(field, nestedEntityType, getJsonPropertyFieldMap(nestedEntityType)));
                }
            }
            current = current.getSuperclass();
        }
        return mappings;
    }

    private static Class<?> resolveNestedEntityType(Field field) {
        Class<?> fieldType = field.getType();
        if (!Collection.class.isAssignableFrom(fieldType)) {
            return null;
        }
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        Type actualType = parameterizedType.getActualTypeArguments()[0];
        if (actualType instanceof Class<?> actualClass && ErpCommonEntity.class.isAssignableFrom(actualClass)) {
            return actualClass;
        }
        if (actualType instanceof ParameterizedType actualParameterizedType
                && actualParameterizedType.getRawType() instanceof Class<?> rawClass
                && ErpCommonEntity.class.isAssignableFrom(rawClass)) {
            return rawClass;
        }
        return null;
    }

    private static void setNestedObjects(Object entity, Map<NestedFieldMapping, JSONObject> nestedObjects) {
        for (Map.Entry<NestedFieldMapping, JSONObject> entry : nestedObjects.entrySet()) {
            JSONObject nestedObject = entry.getValue();
            if (nestedObject.isEmpty()) {
                continue;
            }
            NestedFieldMapping mapping = entry.getKey();
            Object nestedEntity = nestedObject.toJavaObject(mapping.entityType());
            try {
                mapping.collectionField().setAccessible(true);
                mapping.collectionField().set(entity, List.of(nestedEntity));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("ERP嵌套字段赋值失败：" + mapping.collectionField().getName(), e);
            }
        }
    }

    private static Object normalizeErpValue(Object value, Field targetField) {
        if (value instanceof String stringValue && targetField != null
                && !String.class.equals(targetField.getType()) && StrUtil.isBlank(stringValue)) {
            return null;
        }
        if (!(value instanceof JSONArray array) || targetField == null) {
            return value;
        }
        Class<?> targetType = targetField.getType();
        if (targetType.isArray() || Collection.class.isAssignableFrom(targetType) || Object.class.equals(targetType)) {
            return value;
        }
        if (array.isEmpty()) {
            return null;
        }

        // 处理单个值的转换
        Object singleValue = array.size() == 1 ? array.get(0) : value;

        // 对于数值类型，提供更详细的错误信息
        if (targetField.getType() == BigDecimal.class || targetField.getType() == Integer.class ||
            targetField.getType() == Long.class || targetField.getType() == Double.class ||
            targetField.getType() == Float.class) {

            if (singleValue instanceof String strValue) {
                try {
                    if (targetField.getType() == BigDecimal.class) {
                        return new BigDecimal(strValue);
                    } else if (targetField.getType() == Integer.class) {
                        return Integer.parseInt(strValue);
                    } else if (targetField.getType() == Long.class) {
                        return Long.parseLong(strValue);
                    } else if (targetField.getType() == Double.class) {
                        return Double.parseDouble(strValue);
                    } else if (targetField.getType() == Float.class) {
                        return Float.parseFloat(strValue);
                    }
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(String.format(
                        "字段 '%s' 无法将值 '%s' 转换为类型 %s: %s",
                        targetField.getName(), strValue, targetField.getType().getSimpleName(), e.getMessage()
                    ));
                }
            }
        }

        return singleValue;
    }

    private record NestedFieldMapping(Field collectionField, Class<?> entityType, Map<String, Field> fieldMap) {
    }
}
