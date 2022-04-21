package com.zz.gateway.dubbo.core.filter;

import com.zz.gateway.dubbo.core.annotation.DubboApiWrapper;
import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import com.zz.gateway.dubbo.core.context.DubboApiContext;
import com.zz.gateway.dubbo.core.annotation.ResponseReactiveResult;
import com.zz.gateway.dubbo.core.serialize.Serialization;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.PathMatcher;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;

@Slf4j
public class DubboGlobalFilter implements GlobalFilter, Ordered {

    private final PathMatcher pathMatcher;
    private final Serialization serialization;
    private final DubboReferenceConfigProperties dubboReferenceConfigProperties;
    private final ResponseReactiveResult responseResult;
    private ServerCodecConfigurer serverCodecConfigurer;
    private final List<MediaType> supportedTypes = new ArrayList<MediaType>();

    public DubboGlobalFilter(PathMatcher pathMatcher, Serialization serialization,
                             DubboReferenceConfigProperties dubboReferenceConfigProperties, ServerCodecConfigurer serverCodecConfigurer,
                             ResponseReactiveResult responseResult) {
        this.pathMatcher = pathMatcher;
        this.serialization = serialization;
        this.dubboReferenceConfigProperties = dubboReferenceConfigProperties;
        this.serverCodecConfigurer = serverCodecConfigurer;
        this.responseResult = responseResult;
        supportedTypes.add(MediaType.APPLICATION_JSON);
        supportedTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public int getOrder() {
        return 88888;
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
        String path = exchange.getRequest().getPath().value();
        String pathPatternTemp = path;
        DubboApiWrapper dubboApiWrapperTemp = DubboApiContext.MAP_DUBBO_API_WRAPPER.get(path);
        if (null == dubboApiWrapperTemp) {
            for (Map.Entry<String, DubboApiWrapper> entry : DubboApiContext.MAP_DUBBO_API_PATH_PATTERN_WRAPPER
                    .entrySet()) {
                if (pathMatcher.match(entry.getKey(), path)) {
                    dubboApiWrapperTemp = entry.getValue();
                    pathPatternTemp = entry.getKey();
                    break;
                }
            }
        }
        if (null == dubboApiWrapperTemp) {
            throw NotFoundException.create(true, "not find path " + path + " service");
        } else {
            final String pathPattern = pathPatternTemp;
            PathMapping.RequestMethod requestMethod = DubboApiContext.PATTERNS_REQUESTMETHOD.get(pathPattern);
            ServerHttpResponse response = exchange.getResponse();
            String httpMethodName = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "";
            if (!httpMethodName.equals(requestMethod.name())) {
                // 请求Method不匹配
                log.error("path:[{}] requestMethod is fail PathMapping requestMethod:[{}]", pathPattern,
                        requestMethod.name());
                throw new MethodNotAllowedException(requestMethod.name(), null);
            } else {
                final DubboApiWrapper dubboApiWrapper = dubboApiWrapperTemp;
                if (httpMethodName.equals(PathMapping.RequestMethod.POST.name())) {
                    MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
                    if (null == mediaType) {
                        log.error("path:[{}] body param media must application/json or application/x-www-form-urlencoded", pathPattern);
                        throw new UnsupportedMediaTypeStatusException(exchange.getRequest().getHeaders().getContentType(),
                                supportedTypes);
                    }
                    if (mediaType.equals(MediaType.APPLICATION_JSON)
                            || mediaType.equals(MediaType.APPLICATION_JSON_UTF8)) {
                        Object attrBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
                        if (null != attrBody) {
                            NettyDataBuffer nettyDataBuffer = (NettyDataBuffer) attrBody;
                            return responseResult.reactiveFluxResponse(exchange, response,
                                    Flux.just(nettyDataBuffer).flatMap(o -> {
                                        byte[] bytes = new byte[o.readableByteCount()];
                                        o.read(bytes);
                                        DataBufferUtils.release(o);
                                        String bodyString = null;
                                        try {
                                            bodyString = new String(bytes, dubboReferenceConfigProperties.getCharset());
                                        } catch (UnsupportedEncodingException e) {
                                            log.error("fail", e);
                                            throw WebClientResponseException.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, "byte to String fail", null, null, null);
                                        }

                                        return Mono
                                                .fromFuture(dubboApiWrapper.handler(pathPattern, exchange, bodyString))
                                                .flatMap(k -> {
                                                    return Mono.just(response.bufferFactory()
                                                            .wrap(serialization.serializeByte(k)));
                                                });
                                    }));
                        } else {
                            ServerRequest serverRequest = ServerRequest.create(exchange,
                                    serverCodecConfigurer.getReaders());
                            return responseResult.reactiveFluxResponse(exchange, response,
                                    Flux.just(serverRequest.bodyToMono(String.class).defaultIfEmpty("")).flatMap(u -> {
                                        return u.flatMap(o -> {
                                            return Mono
                                                    .fromFuture(
                                                            dubboApiWrapper.handler(pathPattern, exchange, o))
                                                    .flatMap(k -> {
                                                        return Mono.just(response.bufferFactory()
                                                                .wrap(serialization.serializeByte(k)));
                                                    });
                                        });
                                    }));
                        }
                    } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED)) {
                        // form表单形式
                        ServerRequest serverRequest = ServerRequest.create(exchange,
                                serverCodecConfigurer.getReaders());
                        Flux<DataBuffer> fx = Flux.from(serverRequest.formData().flatMap(o -> {
                            return Mono.fromFuture(dubboApiWrapper.handler(pathPattern, exchange, o)).flatMap(k -> {
                                return Mono.just(response.bufferFactory().wrap(serialization.serializeByte(k)));
                            });
                        }));
                        return responseResult.reactiveFluxResponse(exchange, response, fx);
                    } else {
                        log.error("path:[{}] body param media must application/json or application/x-www-form-urlencoded", pathPattern);
                        throw new UnsupportedMediaTypeStatusException(exchange.getRequest().getHeaders().getContentType(),
                                supportedTypes);
                    }
                } else if (httpMethodName.equals(PathMapping.RequestMethod.GET.name())) {
                    Flux<DataBuffer> fx = Flux
                            .from(Mono.fromFuture(dubboApiWrapper.handler(pathPattern, exchange, null)).flatMap(k -> {
                                if (k instanceof CharSequence) {
                                    return Mono.just(response.bufferFactory().wrap(k.toString().getBytes()));
                                }
                                return Mono.just(response.bufferFactory().wrap(serialization.serializeByte(k)));
                            }));

                    return responseResult.reactiveFluxResponse(exchange, response, fx);
                } else {
                    log.error("Only get and post are supported for the time being path:[{}] requestMethod:[{}]",
                            pathPattern, requestMethod.name());
                    throw new MethodNotAllowedException(requestMethod.name(), null);
                }
            }
        }
    }
}
