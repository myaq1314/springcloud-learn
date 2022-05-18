package com.zz.gateway.dubbo.core.filter;

import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.core.client.DefaultDubboProxyService;
import com.zz.gateway.dubbo.core.client.DubboProxyService;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.containsEncodedParts;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-26 15:34
 * ************************************
 */
@Slf4j
@Component
public class DubboGlobalFilterFast implements GlobalFilter, Ordered {
    private DubboArgumentResolver argumentResolver = new DubboArgumentResolver();
    private DubboProxyService proxyService = new DefaultDubboProxyService();
    Encoder<Object> encoder = new Jackson2JsonEncoder();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        // GATEWAY_SCHEME_PREFIX_ATTR属性中RouteToRequestUrlFilter中，order为10000，因此order需要大于10000
        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
        if (url == null
                || !("fast".equals(url.getScheme()) || "fast".equals(schemePrefix))) {
            return chain.filter(exchange);
        }
        log.info("start fast dubbo filter");
        ServerWebExchangeUtils.setAlreadyRouted(exchange);

        // TODO 判断path匹配时优先从转发path取，没有再从原path取
        String path = exchange.getRequest().getURI().getPath();

        DubboApiMetaData metaData = DubboMetaDataManager.get(path);
        // TODO 判断HTTP请求方法是否匹配

        if (null == metaData) {
            // TODO 修改响应
            throw NotFoundException.create(true, "not find path " + path + " service");
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> argumentResolver.resolve(exchange, dataBuffer))
                .flatMap(parameters -> proxyService.invoke(exchange, path, parameters))
                .flatMap(result -> {
                    ServerHttpResponse response = exchange.getResponse();
                    if (result == null) {
                        return Mono.empty();
                    }
                    DataBuffer dataBuffer = null;
                    if (result instanceof java.lang.String) {
                        dataBuffer = response.bufferFactory().wrap(((String) result).getBytes());
                    } else {
                        dataBuffer = encoder.encodeValue(result, response.bufferFactory(), ResolvableType.forClass(String.class), MimeTypeUtils.APPLICATION_JSON, null);
                    }
                    response.getHeaders().add("Content-Type", MimeTypeUtils.APPLICATION_JSON_VALUE);
                    return response.writeWith(Mono.justOrEmpty(dataBuffer));
                })
                .then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 10100;
    }
}
