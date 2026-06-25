package org.jeecg.modules.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ErpRestTemplateConfigTest {

    @Test
    void erpRestTemplateUsesErpTimeoutProperties() {
        ErpConfigProperties properties = new ErpConfigProperties();
        properties.setConnectTimeoutMillis(12345);
        properties.setReadTimeoutMillis(67890);

        RestTemplate restTemplate = new ErpRestTemplateConfig().erpRestTemplate(properties);

        SimpleClientHttpRequestFactory requestFactory = assertInstanceOf(
                SimpleClientHttpRequestFactory.class,
                restTemplate.getRequestFactory());
        assertEquals(12345, ReflectionTestUtils.getField(requestFactory, "connectTimeout"));
        assertEquals(67890, ReflectionTestUtils.getField(requestFactory, "readTimeout"));
    }
}
