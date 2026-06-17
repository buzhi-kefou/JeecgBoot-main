package org.jeecg.modules.erp.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueryDto {

    private Integer format = 1;

    private String useragent = "ApiClient";

    private String timestamp = "2026-06-16 10:01:00";

    private String rid = "202606160002";

    private String v = "1.0";

    private List<QueryDetailDto> parameters;


}