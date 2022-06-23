package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestHeaderMapDubboArgumentResolver implements HandlerDubboArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.HEADER.name().equalsIgnoreCase(paramData.getFromWhere()) &&
                DubboGatewayUtils.isMapType(paramData.getParamType());
    }

    /**
     * 将整个请求头数据转为Map，传入参数中
     */
    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        Map<String, String> result = new LinkedHashMap<>();
        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
        for (Iterator<String> iterator = httpHeaders.keySet().iterator(); iterator.hasNext();) {
            String headerName = iterator.next();
            String headerValue = httpHeaders.getFirst(headerName);
            if (headerValue != null) {
                result.put(headerName, headerValue);
            }
        }
        return result;
    }
}
