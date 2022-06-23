package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.parse.ParamData;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 11:13
 * ************************************
 */
public interface HandlerDubboArgumentResolver {
    boolean supportsParam(ParamData paramData);

    Object resolveArgument(final ServerWebExchange exchange, ParamData paramData);
}
