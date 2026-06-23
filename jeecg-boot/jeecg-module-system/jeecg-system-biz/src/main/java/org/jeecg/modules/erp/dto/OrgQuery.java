package org.jeecg.modules.erp.dto;

import lombok.Data;

@Data
public class OrgQuery {

    private String keyword;

    private Integer pageNo = 1;

    private Integer pageSize = 20;
}
