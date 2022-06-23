package com.zz.scgatewaynew.gatewayfilter;

import com.zz.gateway.common.filter.RequestFilterPostProcessor;
import com.zz.gateway.common.util.GatewayUtil;
import com.zz.sccommon.constant.BizConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.WebFluxSleuthOperators;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-17 16:32
 * ************************************
 */
@Component
@Slf4j
public class GlobalRequestFilter implements GlobalFilter, Ordered {
    @Autowired
    private Tracer tracer;
    @Autowired
    private CurrentTraceContext currentTraceContext;
    @Autowired
    private Optional<List<RequestFilterPostProcessor>> requestFilterPostProcessorList;

    /**
     * 校验请求信息，注入日志id,限流标识到请求头
     * 全局过滤器，在断言之后，特定路由过滤器之前执行
     * 过滤器只有pre和post两个生命周期，即请求前后响应后
     * 在 调用chain.filter 之前的操作是pre, 在then里面的操作是post
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(BizConstants.REQUEST_START_TIME, startTime);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if(route != null) {
            WebFluxSleuthOperators.withSpanInScope(tracer, currentTraceContext, exchange,
                    () -> {
                        log.info(String.format("匹配到的路由信息：{id:%s, routeUrl:%s}", route.getId(), route.getUri()));
                        Object reqBody = GatewayUtil.fetchBody(exchange);
                        if(reqBody != null) {
                            log.info("request body:" + reqBody);
                        }
                    });
        }
        ServerHttpRequest.Builder modifyRequestBuilder = exchange.getRequest().mutate();
        requestFilterPostProcessorList.ifPresent(requestFilterPostProcessors ->
                requestFilterPostProcessors.forEach(p -> p.postProcessBeforeRequest(exchange, modifyRequestBuilder)));

        // 使用chain.filter继续Filter调用链
        return chain.filter(exchange.mutate().request(modifyRequestBuilder.build()).build())
                /*.then(Mono.defer(() ->
                {
                    // then是在调用链中所有的Filter都执行完之后再执行的，所以这里也能获取到路由服务的响应信息
                    // 最后执行的filter的then方法执行优先级越高（比较其他filter的then）
                    log.info("-- record gateway response datestamp");
                    return Mono.empty();
                }))*/;
    }
    
    /**
     * 设置执行顺序，值越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        // 在 SentinelGatewayFilter 之前执行
        return -30;
    }
}
