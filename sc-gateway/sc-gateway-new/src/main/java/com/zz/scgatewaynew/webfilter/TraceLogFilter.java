package com.zz.scgatewaynew.webfilter;

import com.zz.gateway.common.GatewayConstants;
import com.zz.sccommon.constant.BizConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.WebFluxSleuthOperators;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-10-23 16:47
 * ************************************
 */
@Component
@Slf4j
public class TraceLogFilter implements WebFilter, Ordered {
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Autowired
    private Tracer tracer;
    @Autowired
    private CurrentTraceContext currentTraceContext;

    @Override
    public int getOrder() {
        // 必须要在在 TraceWebFilter 之前执行
        return Integer.MIN_VALUE;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(BizConstants.GATEWAY_START_TIME, startTime);

        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String scheme = requestUri.getScheme();

        // Record only http requests (including https)
        if ((!"http".equals(scheme) && !"https".equals(scheme))) {
            return chain.filter(exchange).then(Mono.defer(() -> {
                printTime(exchange);
                return Mono.empty();
            }));
        }

        if (!HttpMethod.POST.matches(exchange.getRequest().getMethodValue())) {
            return chain.filter(exchange).then(Mono.defer(() -> {
                printTime(exchange);
                return Mono.empty();
            }));
        }

        Object cachedBody = exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);
        if (cachedBody != null) {
            return chain.filter(exchange).then(Mono.defer(() -> {
                printTime(exchange);
                return Mono.empty();
            }));
        }

        return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
            final ServerRequest serverRequest = ServerRequest
                    .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders);
            // todo 这里的转换类型可以通过Content-Type来设置
            return serverRequest.bodyToMono((String.class)).doOnNext(objectValue -> {
                exchange.getAttributes().put(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY, objectValue);
            }).then(Mono.defer(() -> {
                ServerHttpRequest cachedRequest = exchange
                        .getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                Assert.notNull(cachedRequest, "cache request shouldn't be null");
                exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                return chain.filter(exchange.mutate().request(cachedRequest).build())
                        .then(Mono.defer(() -> {
                    printTime(exchange);
                    return Mono.empty();
                }));
            }));
        });
    }

    private void printTime(ServerWebExchange exchange) {
        Long startExecTime = exchange.getAttribute(BizConstants.GATEWAY_START_TIME);
        if (startExecTime == null ) {
            return;
        }

        WebFluxSleuthOperators.withSpanInScope(tracer, currentTraceContext, exchange,
                () -> {
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeid = route != null ? route.getId() : "";
                    long end = System.currentTimeMillis();
                    log.info("gateway for routeid [" + routeid + "] total execute time [" + (end - startExecTime) + "] ms");
                });
    }
}
