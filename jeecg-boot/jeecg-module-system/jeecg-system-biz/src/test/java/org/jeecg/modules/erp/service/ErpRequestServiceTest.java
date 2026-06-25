package org.jeecg.modules.erp.service;

import org.jeecg.modules.erp.config.ErpConfigProperties;
import org.jeecg.modules.erp.dto.QueryDetailDto;
import org.jeecg.modules.erp.dto.QueryDto;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.service.ISysInterfaceLogService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpRequestServiceTest {

    @Test
    void requestWritesSuccessInterfaceLogForEveryHttpRequest() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        IErpAuthService authService = mock(IErpAuthService.class);
        ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);
        ErpConfigProperties properties = properties();
        ErpRequestService service = service(restTemplate, authService, interfaceLogService, properties);
        SysInterfaceLog startedLog = new SysInterfaceLog();
        startedLog.setId("log-1");

        when(authService.login()).thenReturn("token-1");
        when(interfaceLogService.start(any(InterfaceLogContext.class))).thenReturn(startedLog);
        when(restTemplate.postForEntity(eq(properties.getQueryUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[[100,\"C\",\"A\"]]"));

        List<ErpOrgEntity> rows = service.request(queryDto(), ErpOrgEntity.class);

        assertEquals(1, rows.size());
        ArgumentCaptor<InterfaceLogContext> contextCaptor = ArgumentCaptor.forClass(InterfaceLogContext.class);
        verify(interfaceLogService).start(contextCaptor.capture());
        InterfaceLogContext context = contextCaptor.getValue();
        assertEquals("ERP_SYNC", context.getBizType());
        assertEquals("ERP接口请求", context.getBizName());
        assertEquals("ErpRequestService", context.getSourceService());
        assertEquals("ORG_Organizations", context.getInterfaceName());
        assertEquals("POST", context.getRequestMethod());
        assertEquals(properties.getQueryUrl(), context.getRequestUrl());
        verify(interfaceLogService).success(eq("log-1"), eq(200), eq("[[100,\"C\",\"A\"]]"), any(Long.class));
    }

    @Test
    void requestWritesFailInterfaceLogAndRethrowsOriginalHttpException() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        IErpAuthService authService = mock(IErpAuthService.class);
        ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);
        ErpConfigProperties properties = properties();
        ErpRequestService service = service(restTemplate, authService, interfaceLogService, properties);
        SysInterfaceLog startedLog = new SysInterfaceLog();
        startedLog.setId("log-1");
        RuntimeException error = new RuntimeException("connect timeout");

        when(authService.login()).thenReturn("token-1");
        when(interfaceLogService.start(any(InterfaceLogContext.class))).thenReturn(startedLog);
        when(restTemplate.postForEntity(eq(properties.getQueryUrl()), any(HttpEntity.class), eq(String.class)))
                .thenThrow(error);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.request(queryDto(), ErpOrgEntity.class));

        assertSame(error, thrown);
        verify(interfaceLogService).fail(eq("log-1"), eq(null), eq(null), eq(error), any(Long.class));
    }

    @Test
    void requestWritesFailInterfaceLogWhenErpResponseParsingFails() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        IErpAuthService authService = mock(IErpAuthService.class);
        ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);
        ErpConfigProperties properties = properties();
        ErpRequestService service = service(restTemplate, authService, interfaceLogService, properties);
        SysInterfaceLog startedLog = new SysInterfaceLog();
        startedLog.setId("log-1");
        String errorBody = "[{\"Result\":{\"ResponseStatus\":{\"IsSuccess\":false,\"Errors\":[{\"Message\":\"单据不存在\"}]}}}]";

        when(authService.login()).thenReturn("token-1");
        when(interfaceLogService.start(any(InterfaceLogContext.class))).thenReturn(startedLog);
        when(restTemplate.postForEntity(eq(properties.getQueryUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(errorBody));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.request(queryDto(), ErpOrgEntity.class));

        assertEquals("ERP请求失败：单据不存在", thrown.getMessage());
        verify(interfaceLogService).fail(eq("log-1"), eq(200), eq(errorBody), eq(thrown), any(Long.class));
    }

    @Test
    void requestUsesSameTraceIdForPaginatedHttpRequests() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        IErpAuthService authService = mock(IErpAuthService.class);
        ISysInterfaceLogService interfaceLogService = mock(ISysInterfaceLogService.class);
        ErpConfigProperties properties = properties();
        ErpRequestService service = service(restTemplate, authService, interfaceLogService, properties);
        SysInterfaceLog firstLog = new SysInterfaceLog();
        firstLog.setId("log-1");
        SysInterfaceLog secondLog = new SysInterfaceLog();
        secondLog.setId("log-2");

        when(authService.login()).thenReturn("token-1");
        when(interfaceLogService.start(any(InterfaceLogContext.class))).thenReturn(firstLog, secondLog);
        when(restTemplate.postForEntity(eq(properties.getQueryUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[[100,\"C\",\"A\"]]"))
                .thenReturn(ResponseEntity.ok("[]"));

        QueryDto queryDto = queryDto();
        queryDto.getParameters().get(0).setLimit(1);
        List<ErpOrgEntity> rows = service.request(queryDto, ErpOrgEntity.class);

        assertEquals(1, rows.size());
        ArgumentCaptor<InterfaceLogContext> contextCaptor = ArgumentCaptor.forClass(InterfaceLogContext.class);
        verify(interfaceLogService, times(2)).start(contextCaptor.capture());
        List<InterfaceLogContext> contexts = contextCaptor.getAllValues();
        assertEquals(contexts.get(0).getTraceId(), contexts.get(1).getTraceId());
        verify(interfaceLogService).success(eq("log-1"), eq(200), eq("[[100,\"C\",\"A\"]]"), any(Long.class));
        verify(interfaceLogService).success(eq("log-2"), eq(200), eq("[]"), any(Long.class));
    }

    private static ErpRequestService service(RestTemplate restTemplate,
                                             IErpAuthService authService,
                                             ISysInterfaceLogService interfaceLogService,
                                             ErpConfigProperties properties) {
        ErpRequestService service = new ErpRequestService();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "erpAuthService", authService);
        ReflectionTestUtils.setField(service, "interfaceLogService", interfaceLogService);
        ReflectionTestUtils.setField(service, "erpConfigProperties", properties);
        return service;
    }

    private static ErpConfigProperties properties() {
        ErpConfigProperties properties = new ErpConfigProperties();
        properties.setQueryUrl("http://erp.example/query");
        properties.setHeaderKey("kdservice-sessionid");
        return properties;
    }

    private static QueryDto queryDto() {
        QueryDetailDto detailDto = new QueryDetailDto();
        detailDto.setFormId("ORG_Organizations");
        detailDto.setFieldKeys(new ErpOrgEntity());
        detailDto.setLimit(2000);
        QueryDto queryDto = new QueryDto();
        queryDto.setParameters(List.of(detailDto));
        return queryDto;
    }
}
