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
public class RequestPathDubboArgumentResolver extends AbstractNameValueArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.PATH.name().equalsIgnoreCase(paramData.getFromWhere()) &&
                !DubboGatewayUtils.isMapType(paramData.getParamType());
    }

    /**
     * 从Path获取指定属性值
     * /order/query/{orderNo}
     * 暂未实现
     */
    @Override
    protected Object resolveName(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("not support argument");
    }

    @Override
    protected void handleMissingValue(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("Required request path params '" + paramData.getParamName() + "' for method parameter type " +
                paramData.getParamType());
    }
}
