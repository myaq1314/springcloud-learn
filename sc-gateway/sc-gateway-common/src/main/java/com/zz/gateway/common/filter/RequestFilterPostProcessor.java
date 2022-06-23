package com.zz.gateway.common.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-10 15:46
 * ************************************
 */
public interface RequestFilterPostProcessor {
    default void postProcessBeforeRequest(ServerWebExchange exchange, ServerHttpRequest.Builder requestBuilder) {

    }

    default void postProcessAfterRequest(ServerWebExchange exchange) {

    }
}
