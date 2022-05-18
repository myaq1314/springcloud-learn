package com.zz.gateway.client.core.parse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-16 11:07
 * ************************************
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DubboApiMetaData {
    private String appName;

    private String path;

    /**
     * 点对点调用的url
     */
    private String url;

    private String pathDesc;

    private String interfaceName;

    private String methodName;

    private ParamData[] params;

    private String version;

    private String group;

    private Integer retries;

    private Integer timeout;

    /**
     * 负载均衡策略
     */
    private String loadBalance;

    /**
     * 接口是否对网关开放
     */
    private boolean enabled;

    /**
     * path:服务名:组名:版本
     */
    public String assembleMetaKey() {
        return String.join(":", this.path, this.appName, this.group, this.version).substring(1);
    }
}
