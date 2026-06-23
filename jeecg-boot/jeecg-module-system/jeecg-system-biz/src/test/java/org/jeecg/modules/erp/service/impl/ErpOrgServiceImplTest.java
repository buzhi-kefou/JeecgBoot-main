package org.jeecg.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.erp.dto.OrgQuery;
import org.jeecg.modules.erp.entity.ErpOrgEntity;
import org.jeecg.modules.erp.mapper.ErpOrgEntityMapper;
import org.jeecg.modules.erp.vo.OrgVo;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErpOrgServiceImplTest {

    @Test
    void getOrgListReturnsPagedOrgOptions() {
        ErpOrgEntityMapper orgMapper = mock(ErpOrgEntityMapper.class);
        when(orgMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(orgPage(1, 20, 2, List.of(org(10L, "ORG-10", "组织十"), org(20L, "ORG-20", "组织二十"))));

        ErpOrgServiceImpl service = new ErpOrgServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", orgMapper);

        OrgQuery query = new OrgQuery();
        query.setKeyword("组织");
        query.setPageNo(1);
        query.setPageSize(20);

        Page<OrgVo> result = service.getOrgList(query);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("10", result.getRecords().get(0).getOrgId());
        assertEquals("ORG-10", result.getRecords().get(0).getOrgCode());
        assertEquals("组织十", result.getRecords().get(0).getOrgName());
    }

    private static Page<ErpOrgEntity> orgPage(long current, long size, long total, List<ErpOrgEntity> records) {
        Page<ErpOrgEntity> page = new Page<>(current, size, total);
        page.setRecords(records);
        return page;
    }

    private static ErpOrgEntity org(Long orgId, String number, String name) {
        ErpOrgEntity entity = new ErpOrgEntity();
        entity.setOrgId(orgId);
        entity.setNumber(number);
        entity.setName(name);
        return entity;
    }
}
