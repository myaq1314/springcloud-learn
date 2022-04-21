package com.zz.gateway.dubbo.core.autoconfig;

import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DubboReferenceConfigProperties.class)
public class DubboGatewayBootstrapConfiguration {

}
