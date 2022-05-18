package com.zz.gateway.dubbo.core.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.common.constant.DubboGatewayConstants;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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
@Component
public class DubboGlobalFilter implements GlobalFilter, Ordered {
    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

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
        // TODO 判断HTTP请求方法是否匹配

        if (null == metaData) {
            // TODO 修改响应
            throw NotFoundException.create(true, "not find path " + path + " service");
        }
        // TODO 解析参数
        ReferenceConfig<GenericService> referenceConfig = MetaDataGenericHandler.get(path);
        if (referenceConfig == null) {
            throw NotFoundException.create(true, "not find path " + path + " service");
        }
        if (!HttpMethod.POST.matches(exchange.getRequest().getMethodValue())) {
            return null;
        }
        return extraBody(exchange).flatMap(obj -> {
            // TODO 引入Gateway common
            Object cachedBody = exchange.getAttribute("cachedRequestBodyObject");
            Pair<String[], Object[]> requestPair;
            ParamData[] paramDatas = metaData.getParams();
            String[] paramTypes = new String[paramDatas.length];

            // TODO 这里先直接把body传进去
            for (int i = 0; i < paramDatas.length; i++) {
                paramTypes[i] = paramDatas[i].getParamType();
            }
            Object[] args = buildParameters((String) cachedBody, paramTypes);
            if (cachedBody == null) {
                // TODO 更新判断GET还是POST

            }
            // 优先确认ReferenceConfig#setAsync设置的是否异步调用参数，如果设置是true，那么这里GenericService的invoke和invokeAsync都是异步的
            // 如果设置的是false，这里的invoke和invokeAsync才会区分是同步响应还是异步响应
            referenceConfig.get().$invoke(metaData.getMethodName(), paramTypes, args);
            Future<Object> result = RpcContext.getServerContext().getFuture();
            CompletableFuture<Object> completableFuture = (result instanceof CompletableFuture ?
                    (CompletableFuture<Object>) result : CompletableFuture.completedFuture(result));
            return Mono.fromFuture(completableFuture.thenApply(ret -> {
                if (Objects.isNull(ret)) {
                    ret = "dubbo server no result";
                }
                exchange.getAttributes().put(DubboGatewayConstants.DUBBO_RESP_RESULT, ret);
                return ret;
            }));
        }).then(chain.filter(exchange));
    }

    private Mono<Object> extraBody(ServerWebExchange exchange) {
        Object cachedBody = exchange.getAttribute("cachedRequestBodyObject");
        if(cachedBody != null) {
            return Mono.empty();
        }
        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

        return serverRequest.bodyToMono(String.class).map(reqBody -> {
            exchange.getAttributes().put("cachedRequestBodyObject", reqBody);
            return Mono.empty();
        });
    }
    /**
     * todo 还缺少多参数的逻辑
     */
    public static Object[] buildParameters(final String body, final String[] parameterTypes) {

        if (parameterTypes.length == 1) {
            return buildSingleParameter(body, parameterTypes[0]);
        }

        return null;
        /*Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
        Object[] objects = paramNameList.stream().map(key -> {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                return GsonUtils.getInstance().convertToMap(obj.toString());
            } else if (obj instanceof JsonArray) {
                return GsonUtils.getInstance().fromList(obj.toString(), Object.class);
            } else {
                return obj;
            }
        }).toArray();
        String[] paramTypes = paramTypeList.toArray(new String[0]);
        return new ImmutablePair<>(paramTypes, objects);*/
    }

    public static Object[] buildSingleParameter(final String body, final String parameterType) {
        if("java.lang.String".equals(parameterType)) {
            System.out.println("use string args");
            return new Object[]{body};
        }
        final Map<String, Object> paramMap = JSON.parseObject(body, new TypeReference<Map<String, Object>>(){});
        for (String key : paramMap.keySet()) {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                paramMap.put(key, JSON.parseObject(body, new TypeReference<Map<String, Object>>(){}));
            } else if (obj instanceof JsonArray) {
                paramMap.put(key, JSON.parseArray(body, Object.class));
            } else {
                paramMap.put(key, obj);
            }
        }
        return new Object[]{paramMap};
    }

    @Override
    public int getOrder() {
        return 10100;
    }
}
