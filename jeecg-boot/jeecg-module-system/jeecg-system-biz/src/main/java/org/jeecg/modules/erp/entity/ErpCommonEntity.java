package org.jeecg.modules.erp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ErpCommonEntity {

    public static String getJsonPropertyNames(Class<?> clazz) {
        return getJsonPropertyNames(clazz, true);
    }

    public static String getJsonPropertyNames(Class<?> clazz, boolean includeNested) {
        return getJsonPropertyNames(clazz, includeNested, new HashSet<>()).stream().distinct().collect(Collectors.joining(","));
    }

    private static List<String> getJsonPropertyNames(Class<?> clazz, boolean includeNested, Set<Class<?>> visitedClasses) {
        if (clazz == null || !visitedClasses.add(clazz)) {
            return List.of();
        }

        List<String> propertyNames = new ArrayList<>();
        JsonPropertyOrder order = clazz.getAnnotation(JsonPropertyOrder.class);
        if (order != null && order.value().length > 0) {
            propertyNames.addAll(Arrays.stream(order.value()).filter(value -> value != null && !value.isBlank()).toList());
        }

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null && jsonProperty.value() != null && !jsonProperty.value().isBlank()) {
                    propertyNames.add(jsonProperty.value());
                }
                if (includeNested) {
                    Class<?> nestedEntityType = resolveNestedEntityType(field);
                    if (nestedEntityType != null) {
                        propertyNames.addAll(getJsonPropertyNames(nestedEntityType, true, visitedClasses));
                    }
                }
            }
            current = current.getSuperclass();
        }
        return propertyNames;
    }

    private static Class<?> resolveNestedEntityType(Field field) {
        Class<?> fieldType = field.getType();
        if (ErpCommonEntity.class.isAssignableFrom(fieldType)) {
            return fieldType;
        }
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

}
