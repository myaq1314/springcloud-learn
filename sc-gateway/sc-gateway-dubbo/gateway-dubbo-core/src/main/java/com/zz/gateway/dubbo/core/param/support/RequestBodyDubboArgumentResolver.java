package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.annotation.ParamFromType;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.common.util.GatewayUtil;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestBodyDubboArgumentResolver extends AbstractBodyArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return ParamFromType.BODY.name().equalsIgnoreCase(paramData.getFromWhere());
    }

    /**
     * 直接将请求body体数据赋值给参数，不解析请求体内容
     */
    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        if("java.lang.String".equals(paramData.getParamType())) {
            return GatewayUtil.fetchBody(exchange);
        }

        return super.resolveArgument(exchange, paramData);
    }
}
