package com.zz.gateway.dubbo.core.param.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zz.gateway.client.core.parse.ParamData;
import com.zz.gateway.common.util.GatewayUtil;
import com.zz.gateway.dubbo.common.constant.DubboGatewayConstants;
import com.zz.gateway.dubbo.core.utils.DubboGatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 16:16
 * ************************************
 */
public abstract class AbstractBodyArgumentResolver implements HandlerDubboArgumentResolver {
    /**
     * 解析请求body转为dubbo参数
     */
    @Override
    public Object resolveArgument(ServerWebExchange exchange, ParamData paramData) {
        String requestBody = GatewayUtil.fetchBody(exchange);
        if(StringUtils.isEmpty(requestBody)) {
            return null;
        }

        if(DubboGatewayUtils.isJsonObject(requestBody)) {
            JSONObject jsonObject = GatewayUtil.fetchBodyJsonObject(exchange);
            if(jsonObject == null) {
                return null;
            }
            if(Boolean.parseBoolean(exchange.getAttribute(DubboGatewayConstants.DUBBO_SINGLE_ARG))) {
                return buildSingleParameter(jsonObject);
            }

            return buildParameters(paramData.getParamName(), jsonObject);
        } else if(DubboGatewayUtils.isJsonArray(requestBody)) {
            List<Object> argVal = JSON.parseArray(requestBody, Object.class);

            return argVal;
        } else {
            return requestBody;
        }
    }

    protected Object buildSingleParameter(JSONObject body) {
        final Map<String, Object> paramMap = new HashMap<>();
        for (String key : body.keySet()) {
            Object obj = body.get(key);
            if (obj instanceof JsonObject) {
                paramMap.put(key, JSON.parseObject(((JSONObject) obj).toJSONString(), new TypeReference<Map<String, Object>>(){}));
            } else if (obj instanceof JsonArray) {
                paramMap.put(key, JSON.parseArray(((JSONArray) obj).toJSONString(), Object.class));
            } else {
                paramMap.put(key, obj);
            }
        }
        return paramMap;
    }

    protected Object buildParameters(String argName, JSONObject body) {
        // 基础类型
        Object argVal = body.get(argName);
        if(argVal instanceof JSONObject) {
            return JSON.parseObject(((JSONObject) argVal).toJSONString(), new TypeReference<Map<String, Object>>(){});
        } else if(argVal instanceof JSONArray) {
            return JSON.parseArray(((JSONArray) argVal).toJSONString(), Object.class);
        } else {
            return argVal;
        }
    }
}
