package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.exception.BizException;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestHeaderDubboArgumentResolver extends AbstractNameValueArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.HEADER.name().equalsIgnoreCase(paramData.getFromWhere()) &&
                !DubboGatewayUtils.isMapType(paramData.getParamType());
    }

    /**
     * 直接从请求头中获取指定属性值
     */
    @Override
    protected Object resolveName(ParamData paramData, ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(paramData.getParamName());
    }

    @Override
    protected void handleMissingValue(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("Required request header '" + paramData.getParamName() + "' for method parameter type " +
                paramData.getParamType());
    }
}
