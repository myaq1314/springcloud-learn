package com.zz.gateway.dubbo.core.filter;

import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.common.util.GatewayUtil;
import com.zz.gateway.dubbo.common.constant.DubboGatewayConstants;
import com.zz.gateway.dubbo.common.exception.BizException;
import com.zz.gateway.dubbo.core.client.DubboProxyService;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import com.zz.gateway.dubbo.core.param.HandlerDubboMethodArgument;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-26 15:34
 * ************************************
 */
@Slf4j
public class DubboGlobalFilter implements GlobalFilter, Ordered {
    private DubboProxyService proxyService;
    private HandlerDubboMethodArgument dubboArgumentHandler;

    public DubboGlobalFilter(DubboProxyService proxyService, HandlerDubboMethodArgument dubboArgumentHandler) {
        this.proxyService = proxyService;
        this.dubboArgumentHandler = dubboArgumentHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        // GATEWAY_SCHEME_PREFIX_ATTR属性中RouteToRequestUrlFilter中，order为10000，因此order需要大于10000
        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
        if (url == null
                || !("dubbo".equals(url.getScheme()) || "dubbo".equals(schemePrefix))) {
            return chain.filter(exchange);
        }
        log.info("start dubbo filter");
        ServerWebExchangeUtils.setAlreadyRouted(exchange);

        // TODO 判断path匹配时优先从转发path取，没有再从原path取
        String path = exchange.getRequest().getURI().getPath();

        DubboApiMetaData metaData = DubboMetaDataManager.get(path);

        if (null == metaData) {
            // TODO 修改响应
            throw new BizException("not find path [" + path + "] dubbo server");
        }

        if(!metaData.isEnabled()) {
            throw new BizException("dubbo service [" + path + "] not activated");
        }

        String requestMethod = exchange.getRequest().getMethodValue();
        if(metaData.getRequestMethod() != null &&
                !metaData.getRequestMethod().contains(requestMethod)) {
            throw new BizException("not support [" + requestMethod + "] request");
        }

        ParamData[] paramDatas = metaData.getParams();

        // TODO 解析参数
        ReferenceConfig<GenericService> referenceConfig = MetaDataGenericHandler.get(path);
        if (referenceConfig == null) {
            throw new BizException("not find path " + path + " service");
        }

        if(HttpMethod.GET.matches(requestMethod)) {
            return proxyService.invoke(exchange, metaData, dubboArgumentHandler.resolve(exchange, paramDatas))
                    .map(ret -> {
                        if (Objects.isNull(ret)) {
                            ret = "dubbo server no result";
                        }
                        exchange.getAttributes().put(DubboGatewayConstants.DUBBO_RESP_RESULT, ret);
                        return ret;
                        })
                    .then(chain.filter(exchange));
        } else if (HttpMethod.POST.matches(requestMethod)) {
            // TODO 加上只支持JSON格式？
            return GatewayUtil.extraBody(exchange)
                    .map(body -> dubboArgumentHandler.resolve(exchange, paramDatas))
                    .flatMap(parameters -> proxyService.invoke(exchange, metaData, parameters))
                    .map(ret -> {
                        if (Objects.isNull(ret)) {
                            ret = "dubbo server no result";
                        }
                        exchange.getAttributes().put(DubboGatewayConstants.DUBBO_RESP_RESULT, ret);
                        return ret;
                    })
                    .then(chain.filter(exchange));
        }
        throw new BizException("not support request method [" + requestMethod + "]");
    }

    @Override
    public int getOrder() {
        return 10100;
    }
}
