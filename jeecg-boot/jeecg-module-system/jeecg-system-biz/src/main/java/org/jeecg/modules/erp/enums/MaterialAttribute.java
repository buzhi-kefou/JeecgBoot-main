package org.jeecg.modules.erp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 物料属性枚举
 */
@Getter
@AllArgsConstructor
public enum MaterialAttribute {

    PURCHASED(1, "外购"),
    SELF_MADE(2, "自制"),
    OUTSOURCED(3, "委外"),
    FEATURE(4, "特征"),
    VIRTUAL(5, "虚拟"),
    SERVICE(6, "服务"),
    ONE_TIME(7, "一次性"),
    CONFIG(9, "配置"),
    ASSET(10, "资产"),
    EXPENSE(11, "费用"),
    MODEL(12, "模型"),
    PRODUCT_SERIES(13, "产品系列");

    private final Integer value;
    private final String label;

    /**
     * 根据 value 获取枚举
     */
    public static MaterialAttribute fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的物料属性值: " + value));
    }

    /**
     * 根据 label 获取枚举
     */
    public static MaterialAttribute fromLabel(String label) {
        return Arrays.stream(values())
                .filter(e -> e.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的物料属性名称: " + label));
    }
}
