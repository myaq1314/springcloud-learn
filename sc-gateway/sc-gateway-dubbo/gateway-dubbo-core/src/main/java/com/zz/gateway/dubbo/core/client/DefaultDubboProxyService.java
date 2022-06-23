package com.zz.gateway.dubbo.core.client;

import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import javafx.util.Pair;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-05-06 14:36
 * ************************************
 */
public class DefaultDubboProxyService implements DubboProxyService {
    @Override
    public Mono<Object> invoke(ServerWebExchange exchange, DubboApiMetaData metaData, Pair<String[], Object[]> args) {
        ReferenceConfig<GenericService> referenceConfig = MetaDataGenericHandler.get(metaData.getPath());
        // 优先确认ReferenceConfig#setAsync设置的是否异步调用参数，如果设置是true，那么这里GenericService的invoke和invokeAsync都是异步的
        // 如果设置的是false，这里的invoke和invokeAsync才会区分是同步响应还是异步响应
        // TODO 使用参数控制同步还是异步，应该放到meta中
        referenceConfig.get().$invoke(metaData.getMethodName(), args.getKey(), args.getValue());
        Future<Object> result = RpcContext.getServerContext().getFuture();
        CompletableFuture<Object> completableFuture = (result instanceof CompletableFuture ?
                (CompletableFuture<Object>) result : CompletableFuture.completedFuture(result));
        return Mono.fromFuture(completableFuture);
    }
}
