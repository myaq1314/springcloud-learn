package com.zz.gateway.common;

/**
 * ************************************
 * create by Intellij IDEA
 * 网关服务专用常量
 *
 * @author Francis.zz
 * @date 2020-04-09 15:11
 * ************************************
 */
public class GatewayConstants {
    /**
     * 配置中心网关数据组
     */
    public static final String GROUP_GATEWAY = "config.gateway";
    /**
     * 动态路由配置的ID
     */
    public static final String DATA_ID_ROUTE = "gateway.route.rule.yaml";
    /**
     * 网关服务参数配置的ID
     */
    public static final String DATA_ID_SETTINGS = "gateway.setting.yaml";
    /**
     * sentinel网关流控配置ID
     */
    public static final String DATA_ID_FLOW = "gateway.sentinel.txt";
    
    /**
     * exchange参数-网关转发失败时给客户端的响应策略
     */
    public static final String CACHE_RESPONSE_BODY = "cacheResponseBody";
    
    /**
     * sp接口响应
     */
    public static final int SP_RESP_STRATEGY = 0;
    /**
     * 订单系统回调通知接口响应
     */
    public static final int ORDER_RESP_STRATEGY = 1;
    /**
     * 微信支付通知接口响应
     */
    public static final int WECHAT_RESP_STRATEGY = 2;

    /**
     * 该值与{@link org.springframework.cloud.gateway.handler.predicate.ReadBodyRoutePredicateFactory}类中的CACHE_REQUEST_BODY_OBJECT_KEY一致
     */
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";
    public static final String CACHE_REQUEST_BODY_JSON_KEY = "cachedRequestBodyJson";

    /**
     * sleuth日志追踪
     */
    public static final String TRACE_ID_NAME = "X-B3-TraceId";
    public static final String SPAN_ID_NAME = "X-B3-SpanId";
    
    /**
     * 记录未找到Route metric信息的resourceName
     * 在{@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}中会用到
     */
    public static final String RESOURCE_FOR_NOROUTE = "resource-no-route";
    
    public static final int PARAM_PARSE_STRATEGY_BODY = 5;

    /** ********** Route metadata start *******************/
    /**
     * 日志/事务追踪字段名
     */
    public static final String META_SESSION_ID = "sessionId";
    /**
     * 后台服务异常时网关响应报文格式
     */
    public static final String META_RESP_FORMAT = "respStrategy";
    /**
     * 扩展数据，JSON格式
     */
    public static final String META_EXT_PARAMS = "extParams";
    /**
     * 流控标识字段，从请求体中提取存入到请求头
     */
    public static final String META_FLOW_CTR = "flowCtrl";
    /** ********** Route metadata end *******************/
}
