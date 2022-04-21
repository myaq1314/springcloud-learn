package com.zz.gateway.dubbo.core.annotation;

import org.springframework.web.server.ServerWebExchange;

import java.util.concurrent.CompletableFuture;

public interface DubboApiWrapper extends BaseApiWrapper {

	CompletableFuture<Object> handler(String pathPattern, ServerWebExchange exchange, Object body);
}
