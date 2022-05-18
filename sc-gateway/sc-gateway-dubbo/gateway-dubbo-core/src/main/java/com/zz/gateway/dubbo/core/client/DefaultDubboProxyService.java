package com.zz.gateway.dubbo.core.client;

import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import com.zz.gateway.dubbo.core.handler.MetaDataGenericHandler;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

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
    public Mono<Object> invoke(ServerWebExchange exchange, String path, Object[] args) {
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
        // TODO 引入Gateway common
        ParamData[] paramDatas = metaData.getParams();
        String[] paramTypes = new String[paramDatas.length];

        // TODO 这里先直接把body传进去
        for (int i = 0; i < paramDatas.length; i++) {
            paramTypes[i] = paramDatas[i].getParamType();
        }

        // 优先确认ReferenceConfig#setAsync设置的是否异步调用参数，如果设置是true，那么这里GenericService的invoke和invokeAsync都是异步的
        // 如果设置的是false，这里的invoke和invokeAsync才会区分是同步响应还是异步响应
        // TODO 使用参数控制同步还是异步，应该放到meta中
        CompletableFuture<Object> result = referenceConfig.get().$invokeAsync(metaData.getMethodName(), paramTypes, args);
        return Mono.fromFuture(result);
    }
}
