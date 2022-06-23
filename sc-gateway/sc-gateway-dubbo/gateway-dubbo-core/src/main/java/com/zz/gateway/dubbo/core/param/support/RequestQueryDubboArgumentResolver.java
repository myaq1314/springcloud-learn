package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.exception.BizException;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestQueryDubboArgumentResolver extends AbstractNameValueArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.QUERY.name().equalsIgnoreCase(paramData.getFromWhere()) &&
                !DubboGatewayUtils.isMapType(paramData.getParamType());
    }

    /**
     * 从Query参数中获取指定属性值
     * /order/query?orderNo=123
     */
    @Override
    protected Object resolveName(ParamData paramData, ServerWebExchange exchange) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
        List<String> queryValue = queryParams.get(paramData.getParamName());

        String result = null;
        if(queryValue != null && queryValue.size() > 0) {
            result = queryValue.get(0);
        }
        return result;
    }

    @Override
    protected void handleMissingValue(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("Required request query params '" + paramData.getParamName() + "' for method parameter type " +
                paramData.getParamType());
    }
}
