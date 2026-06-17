package org.jeecg.modules.erp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("erp")
public class ErpConfigProperties {

    private String authUrl;

    private String queryUrl;

    private String acctID;

    private String appId;

    private String appSecret;

    private String username;

    private String lcid;

    private String headerKey;

    private String headerToken;

//    authUrl: kdservice-sessionid
//    queryUrl: http://192.168.6.134/K3Cloud/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.ExecuteBillQuery.common.kdsvc
//    acctID: '6a044c2a46383c'
//    appId: '347934_RZbrX8HKyvCaR/XJX11CyYUNzi5WSCKO'
//    appSecret: '3fd789c00b9342eea0d7ef3dff5894c0'
//    lcid: '2052'
//    username: '全功能'
//    headerKey: 'kdservice-sessionid'
//    headerValue:

}