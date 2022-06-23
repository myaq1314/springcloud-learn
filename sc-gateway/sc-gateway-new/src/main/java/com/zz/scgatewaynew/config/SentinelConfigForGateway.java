package com.zz.scgatewaynew.config;

import com.alibaba.cloud.sentinel.gateway.scg.SentinelGatewayProperties;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.zz.gateway.common.routedefine.RouteRule;
import com.zz.sccommon.exception.ErrorCode;
import com.zz.sccommon.util.ContextBeanUtil;
import com.zz.scgatewaynew.respdefine.ResponseFactoryService;
import com.zz.scgatewaynew.respdefine.UpstreamResponse;
import com.zz.scgatewaynew.routedefine.GatewayRouteManager;
import com.zz.scgatewaynew.routedefine.RouteNacosProperties;
import com.zz.scgatewaynew.sentinelcustom.CustomSentinelGatewayFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * ************************************
 * create by Intellij IDEA
 * sentinel网关限流配置
 * 单机本地限流
 *
 * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}
 *
 * @author Francis.zz
 * @date 2020-03-18 11:52
 * ************************************
 */

@Configuration
@EnableConfigurationProperties(RouteNacosProperties.class)
@Slf4j
public class SentinelConfigForGateway implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SentinelConfigForGateway.class);
    
    @Autowired
    private ResponseFactoryService responseFactoryService;
    
    @Autowired
    private DefaultListableBeanFactory beanFactory;
    @Autowired
    private Optional<RouteNacosProperties> nacosRouteProperties;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private SentinelGatewayProperties gatewayProperties;
    
    @Bean
    @Order(-1)
    public SentinelGatewayFilter sentinelGatewayFilter() {
        logger.info(
                "[Sentinel SpringCloudGateway] register SentinelGatewayFilter with order: {}",
                gatewayProperties.getOrder());
        return new CustomSentinelGatewayFilter(gatewayProperties.getOrder());
    }
    
/**
     * 注入限流异常处理
     * 可定制限流响应信息，默认为{@link com.alibaba.csp.sentinel.adapter.gateway.sc.callback.DefaultBlockRequestHandler}
     * 使用spring-cloud-alibaba-sentinel-gateway 集成需要创建SPI文件，指定GatewaySlotChainBuilder才能使限流生效
     *
     * @see {@link com.alibaba.cloud.sentinel.gateway.scg.SentinelSCGAutoConfiguration}#blockRequestHandlerOptional
     */

    @Bean
    public BlockRequestHandler blockRequestHandler() {
        return new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange,
                                                      Throwable t) {
                log.info("请求已被限流");
    
                UpstreamResponse.Response failResponseInfo = responseFactoryService.failResponseInfo(exchange, ErrorCode.TOO_MANY_REQUESTS.getReturnMsg(), ErrorCode.TOO_MANY_REQUESTS.getErrorCode());
    
                // JSON result by default.
                return ServerResponse.status(failResponseInfo.getCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(failResponseInfo.getMsg()));
            }
        };
    }
    
    @Bean
    public ContextBeanUtil initBeanUtil() {
        return new ContextBeanUtil();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        nacosRouteProperties.ifPresent(prop -> {
            if(!prop.checkProp()) {
                return;
            }
            try {
                String convertClass = prop.getConverterClass();
                String convertBeanName = "gateway-" + convertClass;
                if (!this.beanFactory.containsBean(convertBeanName)) {
                    this.beanFactory.registerBeanDefinition(convertBeanName,
                            BeanDefinitionBuilder.genericBeanDefinition(Class.forName(convertClass)).getBeanDefinition());
                }
                Converter<String, List<RouteRule>> converter = (Converter<String, List<RouteRule>>) this.beanFactory.getBean(convertBeanName);
                GatewayRouteManager.setEventPublisher(eventPublisher);
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, prop.getServerAddr());
                if(StringUtils.isNotEmpty(prop.getNamespace())) {
                    properties.setProperty(PropertyKeyConst.NAMESPACE, prop.getNamespace());
                }
                GatewayRouteManager.register2Property(
                        new NacosDataSource<List<RouteRule>>(properties, prop.getGroupId(), prop.getDataId(), converter).getProperty());
            } catch (ClassNotFoundException e) {
                log.error("register nacos route bean error", e);
            }
        });
    }
}
