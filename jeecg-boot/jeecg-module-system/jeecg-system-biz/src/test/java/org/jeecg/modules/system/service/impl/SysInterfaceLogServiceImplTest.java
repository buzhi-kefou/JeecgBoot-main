package org.jeecg.modules.system.service.impl;

import org.jeecg.modules.system.dto.InterfaceLogContext;
import org.jeecg.modules.system.entity.SysInterfaceLog;
import org.jeecg.modules.system.mapper.SysInterfaceLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SysInterfaceLogServiceImplTest {

    @Test
    void startPersistsStartedLogAndMasksSensitiveHeaders() {
        SysInterfaceLogMapper mapper = mock(SysInterfaceLogMapper.class);
        SysInterfaceLogServiceImpl service = new SysInterfaceLogServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        InterfaceLogContext context = InterfaceLogContext.builder()
                .traceId("trace-1")
                .bizType("ERP_SYNC")
                .bizName("组织同步")
                .sourceService("ErpRequestService")
                .interfaceName("ORG_Organizations")
                .requestMethod("POST")
                .requestUrl("http://erp.example/query")
                .requestHeaders("{\"Cookie\":\"kdservice-sessionid=secret-token\",\"Authorization\":\"Bearer abc\"}")
                .requestBody("{\"parameters\":[]}")
                .build();

        SysInterfaceLog log = service.start(context);

        ArgumentCaptor<SysInterfaceLog> captor = ArgumentCaptor.forClass(SysInterfaceLog.class);
        verify(mapper).insert(captor.capture());
        SysInterfaceLog saved = captor.getValue();
        assertNotNull(log.getId());
        assertEquals(log.getId(), saved.getId());
        assertEquals("trace-1", saved.getTraceId());
        assertEquals("ERP_SYNC", saved.getBizType());
        assertEquals("ORG_Organizations", saved.getInterfaceName());
        assertFalse(saved.getSuccess());
        assertNotNull(saved.getStartTime());
        assertTrue(saved.getRequestHeaders().contains("***"));
        assertFalse(saved.getRequestHeaders().contains("secret-token"));
        assertFalse(saved.getRequestHeaders().contains("Bearer abc"));
    }

    @Test
    void successUpdatesExistingLog() {
        SysInterfaceLogMapper mapper = mock(SysInterfaceLogMapper.class);
        SysInterfaceLogServiceImpl service = new SysInterfaceLogServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        service.success("log-1", 200, "[[1]]", 123L);

        ArgumentCaptor<SysInterfaceLog> captor = ArgumentCaptor.forClass(SysInterfaceLog.class);
        verify(mapper).updateById(captor.capture());
        SysInterfaceLog updated = captor.getValue();
        assertEquals("log-1", updated.getId());
        assertTrue(updated.getSuccess());
        assertEquals(200, updated.getResponseStatus());
        assertEquals("[[1]]", updated.getResponseBody());
        assertEquals(123L, updated.getCostTime());
        assertNotNull(updated.getEndTime());
    }

    @Test
    void failUpdatesExistingLogWithExceptionInfo() {
        SysInterfaceLogMapper mapper = mock(SysInterfaceLogMapper.class);
        SysInterfaceLogServiceImpl service = new SysInterfaceLogServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        RuntimeException error = new RuntimeException("ERP timeout");

        service.fail("log-1", null, null, error, 456L);

        ArgumentCaptor<SysInterfaceLog> captor = ArgumentCaptor.forClass(SysInterfaceLog.class);
        verify(mapper).updateById(captor.capture());
        SysInterfaceLog updated = captor.getValue();
        assertEquals("log-1", updated.getId());
        assertFalse(updated.getSuccess());
        assertEquals(RuntimeException.class.getName(), updated.getErrorType());
        assertEquals("ERP timeout", updated.getErrorMessage());
        assertEquals(456L, updated.getCostTime());
        assertNotNull(updated.getEndTime());
    }

    @Test
    void loggingStorageFailureDoesNotPropagate() {
        SysInterfaceLogMapper mapper = mock(SysInterfaceLogMapper.class);
        doThrow(new RuntimeException("db down")).when(mapper).insert(any(SysInterfaceLog.class));
        doThrow(new RuntimeException("db down")).when(mapper).updateById(any(SysInterfaceLog.class));
        SysInterfaceLogServiceImpl service = new SysInterfaceLogServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        InterfaceLogContext context = InterfaceLogContext.builder()
                .traceId("trace-1")
                .startTime(new Date())
                .build();

        assertDoesNotThrow(() -> service.start(context));
        assertDoesNotThrow(() -> service.success("log-1", 200, "ok", 1L));
        assertDoesNotThrow(() -> service.fail("log-1", 500, "error", new RuntimeException("boom"), 1L));
    }
}
