package com.zz.gateway.dubbo.core.autoconfig;

import com.zz.gateway.dubbo.core.annotation.DefaultResponseResult;
import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import com.zz.gateway.dubbo.core.filter.DubboGlobalFilter;
import com.zz.gateway.dubbo.core.annotation.ResponseReactiveResult;
import com.zz.gateway.dubbo.core.serialize.JacksonSerialization;
import com.zz.gateway.dubbo.core.serialize.Serialization;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "dubbo.config", name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass({ GlobalFilter.class, DubboReference.class })
public class DubboGatewayAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ResponseReactiveResult responseResult(DubboReferenceConfigProperties dubboReferenceConfigProperties) {
		return new DefaultResponseResult(dubboReferenceConfigProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public DubboGlobalFilter dubboGlobalFilter(ServerCodecConfigurer serverCodecConfigurer, Serialization serialization,
                                               DubboReferenceConfigProperties dubboReferenceConfigProperties, ResponseReactiveResult responseResult, PathMatcher pathMatcher) {
		return new DubboGlobalFilter(pathMatcher, serialization, dubboReferenceConfigProperties, serverCodecConfigurer,
				responseResult);
	}

    @Bean
    @ConditionalOnMissingBean
    public Serialization serialization() {
        return new JacksonSerialization();
    }

    @Bean
    @ConditionalOnMissingBean
    public PathMatcher pathMatcher() {
        return new AntPathMatcher();
    }
}
