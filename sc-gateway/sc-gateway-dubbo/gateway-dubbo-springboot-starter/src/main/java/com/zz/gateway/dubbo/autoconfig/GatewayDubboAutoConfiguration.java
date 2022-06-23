package com.zz.gateway.dubbo.autoconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import com.zz.gateway.dubbo.core.client.DefaultDubboProxyService;
import com.zz.gateway.dubbo.core.client.DubboProxyService;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.filter.DubboGlobalFilter;
import com.zz.gateway.dubbo.core.filter.DubboWriteResponseFilter;
import com.zz.gateway.dubbo.core.handler.DubboMetaDataHandler;
import com.zz.gateway.dubbo.core.handler.DubboRouteLocator;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import com.zz.gateway.dubbo.core.nacos.MetaDataConverter;
import com.zz.gateway.dubbo.core.nacos.MetaDataNacosListener;
import com.zz.gateway.dubbo.core.param.DubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.HandlerDubboMethodArgument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-20 18:09
 * ************************************
 */
@Configuration
@ConditionalOnClass(name = {"com.alibaba.nacos.api.config.ConfigService"})
@Import(GatewayDubboConfig.class)
public class GatewayDubboAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(MetaDataNacosListener.class)
    public MetaDataNacosListener<Map<String, DubboApiMetaData>> dubboMetaDataManager(
            NacosProperties nacosProperties, MetaDataConverter<String, Map<String, DubboApiMetaData>> metaDataConverter,
            DubboMetaDataHandler<DubboApiMetaData> dubboMetaDataHandler, ApplicationEventPublisher eventPublisher) {
        return new MetaDataNacosListener<>(nacosProperties, metaDataConverter,
                new DubboMetaDataManager(dubboMetaDataHandler, eventPublisher)::onChange);
    }

    @Bean
    @ConditionalOnMissingBean(MetaDataConverter.class)
    public MetaDataConverter<String, Map<String, DubboApiMetaData>> dataConverter() {
        return meta -> JSON.parseObject(meta, new TypeReference<Map<String, DubboApiMetaData>>(){});
    }

    @Bean
    @ConditionalOnMissingBean(DubboMetaDataHandler.class)
    public DubboMetaDataHandler<DubboApiMetaData> dubboMetaDataHandler() {
        return new MetaDataGenericHandler();
    }

    @Bean
    public DubboProxyService dubboProxyService() {
        return new DefaultDubboProxyService();
    }

    @Bean
    public HandlerDubboMethodArgument handlerDubboMethodArgument() {
        return new DubboArgumentResolver();
    }

    @Bean
    public DubboGlobalFilter dubboGlobalFilter(DubboProxyService dubboProxyService, HandlerDubboMethodArgument handlerDubboMethodArgument) {
        return new DubboGlobalFilter(dubboProxyService, handlerDubboMethodArgument);
    }

    @Bean
    public DubboWriteResponseFilter dubboWriteResponseFilter(
            GatewayProperties properties) {
        return new DubboWriteResponseFilter(properties.getStreamingMediaTypes());
    }

    @Bean
    @ConditionalOnClass(name = {"org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder"})
    public RouteLocator dubboRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return new DubboRouteLocator(routeLocatorBuilder);
    }
}
