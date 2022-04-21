package com.zz.gateway.client.autoconfig;

import com.zz.gateway.client.core.common.GatewayDubboClientConfig;
import com.zz.gateway.client.core.register.DubboSpringBeanListener;
import com.zz.gateway.client.core.register.NacosClientRegisterRepository;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-14 10:54
 * ************************************
 */
@Configuration
@ConditionalOnClass(name = {"com.alibaba.nacos.api.config.ConfigService", "org.apache.dubbo.config.spring.ServiceBean"})
@Import(GatewayDubboClientConfiguration.class)
public class DubboClientAutoConfiguration {
    @Bean
    public DubboSpringBeanListener apacheDubboServiceBeanListener(GatewayDubboClientConfig gatewayDubboClientConfig,
                                                                  NacosClientRegisterRepository nacosClientRegisterRepository) {
        return new DubboSpringBeanListener(gatewayDubboClientConfig, nacosClientRegisterRepository);
    }

    @Bean
    public NacosClientRegisterRepository nacosClientRegisterRepository(NacosProperties nacosProperties) {
        return new NacosClientRegisterRepository(nacosProperties);
    }
}
