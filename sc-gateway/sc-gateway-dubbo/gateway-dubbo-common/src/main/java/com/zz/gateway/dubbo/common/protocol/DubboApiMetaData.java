package com.zz.gateway.dubbo.common.protocol;

import lombok.Builder;
import lombok.Data;

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

    private String parameterTypes;

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

    public String assembleMetaKey() {
        return String.join(":", this.path, this.version, this.group, this.appName).replaceAll("/", ".").substring(1);
    }
}
