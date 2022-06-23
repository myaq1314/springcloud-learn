package com.zz.scgatewaynew.gatewayfilter;

import com.alibaba.fastjson.JSONObject;
import com.zz.gateway.common.GatewayConstants;
import com.zz.gateway.common.filter.RequestFilterPostProcessor;
import com.zz.gateway.common.util.GatewayUtil;
import com.zz.sccommon.constant.BizConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-13 10:43
 * ************************************
 */
@Component
@Order(0)
public class JsonBodyRequestPostProcessor implements RequestFilterPostProcessor {
    private final static Logger log = LoggerFactory.getLogger(JsonBodyRequestPostProcessor.class);

    @Override
    public void postProcessBeforeRequest(ServerWebExchange exchange, ServerHttpRequest.Builder modifyRequestBuilder) {
        JSONObject jsonObject = GatewayUtil.fetchBodyJsonObject(exchange);
        if(jsonObject == null) {
            return;
        }
        if(modifyRequestBuilder == null) {
            modifyRequestBuilder = exchange.getRequest().mutate();
        }
        // 接口类型标识
        String command = GatewayUtil.fromJsonBodyWithMeta(exchange, GatewayConstants.META_FLOW_CTR);
        if(StringUtils.isNotEmpty(command)) {
            modifyRequestBuilder.header(BizConstants.COMMAND_ID, command);
        }

        String sessionId = GatewayUtil.fromJsonBodyWithMeta(exchange, GatewayConstants.META_SESSION_ID);
        // 流控标识，通过请求body中是否有sessionid判断是否需要合并请求次数
        String flowCtrlFlag = "true";
        if(StringUtils.isNotEmpty(sessionId)) {
            // 请求body中没有transactionid参数则判断为一次独立的请求
            flowCtrlFlag = "false";
            modifyRequestBuilder.header(BizConstants.HEADER_TRACE_ID, sessionId);
        }

        modifyRequestBuilder.header(BizConstants.FLOW_CTRL_FLAG, flowCtrlFlag);
    }
}
