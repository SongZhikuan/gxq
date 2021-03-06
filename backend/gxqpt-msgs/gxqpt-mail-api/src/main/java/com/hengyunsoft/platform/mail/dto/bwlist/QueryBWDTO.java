package com.hengyunsoft.platform.mail.dto.bwlist;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zjr
 * @date 2018/08/08
 */
@Data
public class QueryBWDTO implements Serializable {

    /**
     * 名单类型 black、黑名单 white、白名单
     * @mbggenerated
     */
    @ApiModelProperty(value = "名单类型 black、黑名单 white、白名单")
    private String bwType;

    /**
     * 地址类型 email、邮件 ip、IP field、域名
     * @mbggenerated
     */
    @ApiModelProperty(value = "地址类型 email、邮件 ip、IP field、域名")
    private String locateType;

}
