package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:35
 * ************************************
 */
public class DefaultDubboArgumentResolver extends AbstractBodyArgumentResolver {
    private final HandlerDubboArgumentResolver queryResolver = new RequestQueryDubboArgumentResolver();
    private final HandlerDubboArgumentResolver queryMapResolver = new RequestQueryMapDubboArgumentResolver();

    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        String requestMethod = exchange.getRequest().getMethodValue();
        if(HttpMethod.POST.matches(requestMethod)) {
            return super.resolveArgument(exchange, paramData);
        }
        // GET请求从请求Query参数中取值
        if(!DubboGatewayUtils.isMapType(paramData.getParamType())) {
            return queryResolver.resolveArgument(exchange, paramData);
        } else {
            return queryMapResolver.resolveArgument(exchange, paramData);
        }
    }

    @Override
    public boolean supportsParam(ParamData paramData) {
        return StringUtils.isEmpty(paramData.getFromWhere());
    }
}
