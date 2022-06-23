package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.parse.ParamData;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 11:15
 * ************************************
 */
public class HandlerDubboArgumentResolverComposite implements HandlerDubboArgumentResolver {
    private final List<HandlerDubboArgumentResolver> argumentResolvers = new ArrayList<>();

    private final Map<ParamData, HandlerDubboArgumentResolver> argumentResolverCache = new ConcurrentHashMap<>(256);

    public HandlerDubboArgumentResolverComposite addResolver(HandlerDubboArgumentResolver resolver) {
        this.argumentResolvers.add(resolver);
        return this;
    }

    public HandlerDubboArgumentResolverComposite addResolvers(@Nullable List<? extends HandlerDubboArgumentResolver> resolvers) {
        if (resolvers != null) {
            this.argumentResolvers.addAll(resolvers);
        }
        return this;
    }

    public HandlerDubboArgumentResolverComposite addResolvers(@Nullable HandlerDubboArgumentResolver... resolvers) {
        if (resolvers != null) {
            Collections.addAll(this.argumentResolvers, resolvers);
        }
        return this;
    }

    public List<HandlerDubboArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(this.argumentResolvers);
    }

    public void clear() {
        this.argumentResolvers.clear();
        this.argumentResolverCache.clear();
    }

    @Override
    public boolean supportsParam(ParamData paramData) {
        return getArgumentResolver(paramData) != null;
    }

    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        HandlerDubboArgumentResolver resolver = getArgumentResolver(paramData);
        if (resolver == null) {
            throw new IllegalArgumentException("Unsupported parameter " + paramData.toString());
        }
        return resolver.resolveArgument(exchange, paramData);
    }

    private HandlerDubboArgumentResolver getArgumentResolver(ParamData parameter) {
        HandlerDubboArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            for (HandlerDubboArgumentResolver resolver : this.argumentResolvers) {
                if (resolver.supportsParam(parameter)) {
                    result = resolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }

        return result;
    }
}
