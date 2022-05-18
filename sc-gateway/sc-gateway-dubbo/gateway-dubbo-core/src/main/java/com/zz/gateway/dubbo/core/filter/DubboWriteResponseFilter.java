package com.zz.gateway.dubbo.core.filter;

import com.zz.gateway.dubbo.common.constant.DubboGatewayConstants;
import com.zz.gateway.dubbo.common.utils.JsonParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-28 11:19
 * ************************************
 */
@Slf4j
@Component
public class DubboWriteResponseFilter implements GlobalFilter, Ordered {
    public static final int WRITE_RESPONSE_FILTER_ORDER = -1;

    private final List<MediaType> streamingMediaTypes;

    public DubboWriteResponseFilter(List<MediaType> streamingMediaTypes) {
        this.streamingMediaTypes = streamingMediaTypes;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnError(throwable -> cleanup(exchange))
                .then(Mono.defer(() -> {
                    Object result = exchange.getAttribute(DubboGatewayConstants.DUBBO_RESP_RESULT);

                    if (result == null) {
                        return Mono.empty();
                    }
                    ServerHttpResponse response = exchange.getResponse();

                    MediaType contentType = null;
                    try {
                        contentType = response.getHeaders().getContentType();
                    }
                    catch (Exception e) {
                        log.warn("invalid media type", e);
                    }
                    // TODO 响应的解析转换
                    DataBuffer resultData = null;
                    if (result instanceof java.lang.String) {
                        resultData = response.bufferFactory().wrap(((String) result).getBytes());
                    } else {
                        resultData = response.bufferFactory().wrap(JsonParseUtil.toJson(result).getBytes());
                    }

                    response.getHeaders().add("Content-Type", MimeTypeUtils.APPLICATION_JSON_VALUE);
                    return (isStreamingMediaType(contentType)
                            ? response.writeAndFlushWith(Flux.just(resultData).map(Flux::just))
                            : response.writeWith(Mono.just(resultData)));
                })).doOnCancel(() -> cleanup(exchange));
    }

    @Override
    public int getOrder() {
        return WRITE_RESPONSE_FILTER_ORDER;
    }

    private void cleanup(ServerWebExchange exchange) {

    }

    private boolean isStreamingMediaType(@Nullable MediaType contentType) {
        return (contentType != null && this.streamingMediaTypes.stream()
                .anyMatch(contentType::isCompatibleWith));
    }
}
