package com.zz.gateway.client.autoconfig;

import com.zz.gateway.client.core.common.GatewayDubboClientConfig;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-15 16:33
 * ************************************
 */
public class GatewayDubboClientConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "nacos.config")
    public NacosProperties nacosProperties() {
        return new NacosProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "gateway.client")
    public GatewayDubboClientConfig gatewayDubboClientConfig() {
        return new GatewayDubboClientConfig();
    }
}
