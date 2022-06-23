package com.zz.gateway.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zz.gateway.common.GatewayConstants;
import com.zz.sccommon.constant.BizConstants;
import com.zz.sccommon.util.UuidUtils;
import com.zz.sccommon.util.sign.RSASignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-05-31 11:05
 * ************************************
 */
@Slf4j
public class GatewayUtil {
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    /**
     * 该值与{@link ReadBodyPredicateFactory}类中的CACHE_REQUEST_BODY_OBJECT_KEY一致
     */
    //public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    /**
     * 从ServerWebExchange缓存中提取traceId,如果没有只保存一个新值
     * 参考ServerWebExchange.LOG_ID_ATTRIBUTE
     *
     * @param
     * @return
     */
    public static String getTraceIdFromCache(ServerWebExchange exchange) {
        String traceId = exchange.getAttribute(BizConstants.MDC_TRACE_ID);
        if(StringUtils.isEmpty(traceId)) {
            traceId = UuidUtils.generateUuid();
            exchange.getAttributes().put(BizConstants.MDC_TRACE_ID, traceId);
        }

        return traceId;
    }

    public static String formatRequest(ServerHttpRequest request) {
        String rawQuery = request.getURI().getRawQuery();
        String query = StringUtils.isNotBlank(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.getMethodValue() + " \"" + request.getPath() + query + "\"";
    }

    /**
     * 判断请求头如果有签名信息，则把响应数据加签后放入响应头
     *
     * @param exchange
     * @param body
     * @param privateKeyStr
     * @return flag 请求头签名标识
     */
    public static boolean wrapRespHeaderWithSign(ServerWebExchange exchange, Map<String, Object> body, String privateKeyStr) {
        boolean flag = false;
        String requestHeaderSignValue = exchange.getRequest().getHeaders().getFirst(BizConstants.SIGNATURE_VALUE);
        if (requestHeaderSignValue != null) {
            exchange.getResponse().getHeaders().add(BizConstants.SIGNATURE_TYPE, RSASignatureUtil.SIGN_ALGORITHMS_SHA256);
            exchange.getResponse().getHeaders().add(BizConstants.SIGNATURE_VALUE,
                    RSASignatureUtil.sign(JSON.toJSONString(body), privateKeyStr, "UTF-8", RSASignatureUtil.SIGN_ALGORITHMS_SHA256));

            flag = true;
        }
        return flag;
    }

    /**
     * 获取request body, 默认POST请求都已调用readBody predicate
     *
     * @param exchange
     * @return
     */
    public static <T> T fetchBody(final ServerWebExchange exchange) {
        if(exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY) == null) {
            return null;
        }

        return exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);
    }

    /**
     * 获取request body, 默认POST请求都已调用readBody predicate
     *
     * @param exchange
     * @return
     */
    public static JSONObject fetchBodyJsonObject(final ServerWebExchange exchange) {
        JSONObject result = exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_JSON_KEY);
        if(result != null) {
            return result;
        }

        String body = fetchBody(exchange);
        if(StringUtils.isNotEmpty(body)) {
            if(body.startsWith("{") && body.endsWith("}")) {
                result = JSON.parseObject(body);
                exchange.getAttributes().put(GatewayConstants.CACHE_REQUEST_BODY_JSON_KEY, result);
            }
        }

        return result;
    }

    /**
     * 校验请求是否为json格式
     *
     * @param contentType
     * @return
     */
    public static boolean isJson(MediaType contentType) {
        return MediaType.APPLICATION_JSON.equals(contentType) || MediaType.APPLICATION_JSON_UTF8.equals(contentType);
    }

    public static Mono<Object> extraBody(final ServerWebExchange exchange) {
        Object cachedBody = exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);

        if(cachedBody != null) {
            return Mono.just(cachedBody);
        }

        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

        return serverRequest.bodyToMono(String.class).map(reqBody -> {
            exchange.getAttributes().put(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY, reqBody);
            return reqBody;
        });
    }

    public static <T, V> Mono<T> cacheBody(ServerWebExchange exchange, Class<V> inClazz, Function<V, Mono<T>> function) {
        V cachedBody = exchange.getAttribute(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY);

        if(cachedBody != null) {
            return function.apply(cachedBody);
        }

        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

        return serverRequest.bodyToMono(inClazz).map(reqBody -> {
            exchange.getAttributes().put(GatewayConstants.CACHE_REQUEST_BODY_OBJECT_KEY, reqBody);
            return reqBody;
        }).flatMap(function)
          .doOnError(error -> log.warn("read and parse request body error", error));
    }

    public static String routeMetadata(final ServerWebExchange exchange, String metaKey) {
        Route curRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if(curRoute == null) {
            return null;
        }
        Map<String, Object> metadata = curRoute.getMetadata();
        if(metadata == null) {
            return null;
        }

        String value = (String) metadata.get(metaKey);

        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJsonBody(final ServerWebExchange exchange, String key) {
        JSONObject jsonObject = fetchBodyJsonObject(exchange);
        if(jsonObject == null) {
            return null;
        }
        Object value = jsonObject.get(key);
        if(value == null) {
            return null;
        }

        return (T) value;
    }

    /**
     * 从Request Json Body中获取路由Metadata指定的属性
     *
     * @param exchange
     * @param metaKey
     * @return
     */
    public static String fromJsonBodyWithMeta(final ServerWebExchange exchange, String metaKey) {
        String fieldName = GatewayUtil.routeMetadata(exchange, metaKey);
        if(StringUtils.isEmpty(fieldName)) {
            return null;
        }
        String bodyValue = GatewayUtil.fromJsonBody(exchange, fieldName);

        return bodyValue;
    }
}
