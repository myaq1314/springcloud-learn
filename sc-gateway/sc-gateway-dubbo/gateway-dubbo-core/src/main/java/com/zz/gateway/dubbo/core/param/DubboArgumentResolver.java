package com.zz.gateway.dubbo.core.param;

import com.google.common.collect.Lists;
import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.constant.DubboGatewayConstants;
import com.zz.gateway.dubbo.core.param.support.DefaultDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.HandlerDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.HandlerDubboArgumentResolverComposite;
import com.zz.gateway.dubbo.core.param.support.RequestBodyDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestCookieDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestHeaderDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestHeaderMapDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestPathDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestQueryDubboArgumentResolver;
import com.zz.gateway.dubbo.core.param.support.RequestQueryMapDubboArgumentResolver;
import javafx.util.Pair;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-05-06 14:36
 * ************************************
 */
public class DubboArgumentResolver implements HandlerDubboMethodArgument, InitializingBean {
    private HandlerDubboArgumentResolverComposite argumentResolvers;

    public List<HandlerDubboArgumentResolver> getArgumentResolvers() {
        return (this.argumentResolvers != null ? this.argumentResolvers.getResolvers() : null);
    }

    public void setArgumentResolvers(List<HandlerDubboArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = new HandlerDubboArgumentResolverComposite();
        }
        this.argumentResolvers.addResolvers(argumentResolvers);
    }

    @Override
    public Pair<String[], Object[]> resolve(final ServerWebExchange exchange, final ParamData[] paramDatas) {
        if(paramDatas == null || paramDatas.length == 0) {
            return new Pair<>(new String[0], new Object[0]);
        }
        checkArguments(exchange, paramDatas);

        Object[] args = new Object[paramDatas.length];
        String[] argTypes = new String[paramDatas.length];
        for (int i = 0; i < paramDatas.length; i++) {
            ParamData paramData = paramDatas[i];
            if(StringUtils.isEmpty(paramData.getParamType()) || StringUtils.isEmpty(paramData.getParamName())) {
                throw new IllegalStateException("dubbo args unknown");
            }
            if (!this.argumentResolvers.supportsParam(paramData)) {
                throw new IllegalStateException(formatArgumentError(paramData, "No suitable resolver"));
            }
            try {
                args[i] = this.argumentResolvers.resolveArgument(exchange, paramData);
                argTypes[i] = paramData.getParamType();
            }
            catch (Exception ex) {
                throw ex;
            }
            if(args[i] == null) {
                if(paramData.isRequired()) {
                    throw new IllegalStateException(formatArgumentError(paramData, "arg missing"));
                }
            }
        }

        return new Pair<>(argTypes, args);
    }

    protected static String formatArgumentError(ParamData paramData, String message) {
        return "Could not resolve parameter [" + paramData.getParamName() + "] in " +
                paramData.getMethodName() + ":" + message;
    }

    private List<HandlerDubboArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerDubboArgumentResolver> argumentResolvers = Lists.newArrayList(
                new RequestPathDubboArgumentResolver(),
                new RequestCookieDubboArgumentResolver(),
                new RequestHeaderDubboArgumentResolver(),
                new RequestHeaderMapDubboArgumentResolver(),
                new RequestQueryDubboArgumentResolver(),
                new RequestQueryMapDubboArgumentResolver(),
                new RequestBodyDubboArgumentResolver(),
                new DefaultDubboArgumentResolver()
        );

        return argumentResolvers;
    }

    private void checkArguments(final ServerWebExchange exchange, final ParamData[] paramDatas) {
        Boolean isSingle = null;
        for (ParamData paramData : paramDatas) {
            if(StringUtils.isEmpty(paramData.getFromWhere()) ||
                    ParamFromType.BODY.name().equals(paramData.getFromWhere())) {
                if(BooleanUtils.toBoolean(isSingle)) {
                    isSingle = false;
                    break;
                } else {
                    isSingle = true;
                }
            }
        }
        if(BooleanUtils.toBoolean(isSingle)) {
            exchange.getAttributes().put(DubboGatewayConstants.DUBBO_SINGLE_ARG, "true");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.argumentResolvers == null) {
            List<HandlerDubboArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerDubboArgumentResolverComposite().addResolvers(resolvers);
        }
    }
}
