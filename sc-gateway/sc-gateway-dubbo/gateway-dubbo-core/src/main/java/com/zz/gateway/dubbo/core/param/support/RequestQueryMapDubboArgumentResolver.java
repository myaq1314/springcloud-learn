package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestQueryMapDubboArgumentResolver implements HandlerDubboArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.QUERY.name().equalsIgnoreCase(paramData.getFromWhere()) &&
                DubboGatewayUtils.isMapType(paramData.getParamType());
    }

    /**
     * 将整个请求Query参数转为Map
     */
    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();

        Map<String, String> result = new LinkedHashMap<>();
        for (Iterator<String> iterator = queryParams.keySet().iterator(); iterator.hasNext();) {
            String queryName = iterator.next();
            List<String> queryValue = queryParams.get(queryName);
            if (queryValue != null && queryValue.size() > 0) {
                result.put(queryName, queryValue.get(0));
            }
        }
        return result;
    }
}
