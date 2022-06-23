package com.zz.gateway.dubbo.core.handler;

import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.common.routedefine.RouteRule;
import com.zz.gateway.dubbo.core.context.DubboMetaDataManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 18:18
 * ************************************
 */
@Slf4j
public class DubboRouteLocator implements RouteLocator {
    private RouteLocatorBuilder routeLocatorBuilder;
    private volatile Flux<Route> currentRoute = Flux.empty();

    public DubboRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        this.routeLocatorBuilder = routeLocatorBuilder;
    }

    /**
     * GatewayRoutePropertyListener 会发布 RefreshRoutesEvent 事件，
     * {@link org.springframework.cloud.gateway.route.CachingRouteLocator} 会监听该事件，然后调用fetch来刷新所有路由。
     */
    @Override
    public Flux<Route> getRoutes() {
        Map<String, DubboApiMetaData> metaDataMap = DubboMetaDataManager.getCache();
        if(CollectionUtils.isEmpty(metaDataMap)) {
            log.info("not found dubbo route");
            return Flux.empty();
        }

        try {
            RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
            for (Map.Entry<String, DubboApiMetaData> entry : metaDataMap.entrySet()) {
                DubboApiMetaData metaData = entry.getValue();
                builder = builder.route(metaData.assembleMetaKey(), p ->
                        p.path(metaData.getPath())
                                .uri("dubbo://127.0.0.1"));
            }
            currentRoute = builder.build().getRoutes()
                    .doOnNext(route -> log.info("注册dubbo路由id:" + route.getId()));
        } catch (Exception e) {
            log.error("build dubbo route error ", e);
        }
        
        return currentRoute;
    }
}
