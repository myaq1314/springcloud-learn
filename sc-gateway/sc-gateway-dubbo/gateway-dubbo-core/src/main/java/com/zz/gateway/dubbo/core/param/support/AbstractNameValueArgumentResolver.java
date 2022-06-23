package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.exception.BizException;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 16:16
 * ************************************
 */
public abstract class AbstractNameValueArgumentResolver implements HandlerDubboArgumentResolver {
    /**
     * 解析内容，获取参数名对应的属性值
     *
     * @param exchange
     * @param paramData
     * @return
     */
    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        Object argValue = resolveName(paramData, exchange);

        if(argValue == null && paramData.isRequired()) {
            handleMissingValue(paramData, exchange);
        }
        return argValue;
    }

    @Nullable
    protected abstract Object resolveName(ParamData paramData, ServerWebExchange exchange);

    protected void handleMissingValue(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("Missing argument '" + paramData.getParamName() +
                "' for method parameter of type " + paramData.getParamType());
    }
}
