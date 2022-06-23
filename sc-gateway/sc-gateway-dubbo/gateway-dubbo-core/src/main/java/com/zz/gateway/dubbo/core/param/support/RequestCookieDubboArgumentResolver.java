package com.zz.gateway.dubbo.core.param.support;

import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.exception.BizException;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 * 从Cookie中获取属性值
 * 暂未实现
 *
 * @author Francis.zz
 * @date 2022-06-16 14:44
 * ************************************
 */
public class RequestCookieDubboArgumentResolver extends AbstractNameValueArgumentResolver {
    @Override
    public boolean supportsParam(ParamData paramData) {
        return false;
    }

    @Override
    protected Object resolveName(ParamData paramData, ServerWebExchange exchange) {
        throw new BizException("not support argument");
    }
}
