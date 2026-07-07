package org.jeecg.modules.erp.dto;

import lombok.Data;

/**
 * 生产订单查询条件
 */
@Data
public class ProductionOrderQuery {

    private String billNo;

    private String prdOrgId;

    private String materialId;

    private String planStartBegin;

    private String planStartEnd;

    private String planFinishBegin;

    private String planFinishEnd;

    private Integer pageNo = 1;

    private Integer pageSize = 10;
}
