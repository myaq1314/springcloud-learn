package com.zz.gateway.dubbo.core.client;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-05-06 14:36
 * ************************************
 */
public interface DubboProxyService {
    Mono<Object> invoke(ServerWebExchange exchange, String path, Object[] args);
}
