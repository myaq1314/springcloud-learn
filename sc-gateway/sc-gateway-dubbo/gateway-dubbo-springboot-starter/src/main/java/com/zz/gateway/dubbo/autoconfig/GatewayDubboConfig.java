package com.zz.gateway.dubbo.autoconfig;

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
public class GatewayDubboConfig {
    @Bean
    @ConfigurationProperties(prefix = "nacos.config")
    public NacosProperties nacosPropertiesConfig() {
        return new NacosProperties();
    }
}
