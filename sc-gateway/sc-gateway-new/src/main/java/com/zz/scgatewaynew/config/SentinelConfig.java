package com.zz.scgatewaynew.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zz.gateway.common.nacos.entity.ApiDefinitionEntity;
import com.zz.gateway.common.nacos.entity.GatewayFlowRuleEntity;
import com.zz.gateway.common.nacos.entity.RuleEntityWrapper;
import com.zz.gateway.common.routedefine.RouteRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-01 17:52
 * ************************************
 */
@Configuration
@Slf4j
public class SentinelConfig {
    @Bean("gateway-routeRuleDecoder")
    public Converter<String, List<RouteRule>> routeRuleDecoder() {
        return s -> {
            log.debug("[nacos] route config json:" + s);
            RuleEntityWrapper<RouteRule> apiEntity = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<RouteRule>>(){});

            if(apiEntity == null) {
                return new ArrayList<>();
            }
            return apiEntity.getRuleEntity();
        };
    }

    /**
     * 网关限流动态规则转换，sentinel-console配置并存储在nacos， 配置参考
     * <pre>
     * [
     *     {
     *       "resource": "customized_api1",
     *       "resourceMode": 1,
     *       "count": 1.0,
     *       "intervalSec": 5.0,
     *       "paramItem": {
     *           "parseStrategy": 2,
     *           "fieldName": "flowctrlflag",
     *           "pattern": "true",
     *           "matchStrategy": 0
     *       }
     *     }
     * ]
     * </pre>
     * 存储实体类：GatewayFlowRuleEntity
     * beanName必须使用“sentinel-”开头，后面字符串则为application.yml中的converter-class配置
     * <code>
     *     converter-class: gatewayFlowDecoder
     *     data-type: custom
     * </code>
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler}
     * @see {@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayRuleCommandHandler} sentinel-client接收处理动态Flow rule实现
     */

    @Bean("sentinel-gatewayFlowDecoder")
    public Converter<String, Set<GatewayFlowRule>> gatewayFlowDecoder() {
        return s -> {
            RuleEntityWrapper<GatewayFlowRuleEntity> ruleEntityWrapper = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<GatewayFlowRuleEntity>>(){});
            if(ruleEntityWrapper == null) {
                return new HashSet<>();
            }
            return ruleEntityWrapper.getRuleEntity().stream().map(GatewayFlowRuleEntity::toGatewayFlowRule).collect(Collectors.toSet());
        };
    }

    /**
     * API分组动态配置参考
     * <pre>
     * [
     *      {
     *          "apiName": "customized_api1",
     *          "predicateItems": [{
     *              "pattern": "/dispatcher",
     *              "matchStrategy": 0
     *          }]
     *      }
     * ]
     * </pre>
     * @see {@link com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler}
     * 存储实体类：ApiDefinitionEntity
     * beanName必须使用“sentinel-”开头
     * 转换参考：{@link com.alibaba.csp.sentinel.adapter.gateway.common.command.UpdateGatewayApiDefinitionGroupCommandHandler#parseJson}
     */

    @Bean("sentinel-apiDefinitionDecoder")
    public Converter<String, Set<ApiDefinition>> apiDefinitionDecoder() {
        return s -> {
            RuleEntityWrapper<ApiDefinitionEntity> apiEntity = JSON.parseObject(s, new TypeReference<RuleEntityWrapper<ApiDefinitionEntity>>(){});
            if(apiEntity == null) {
                return new HashSet<>();
            }
            return apiEntity.getRuleEntity().stream().map(ApiDefinitionEntity::toApiDefinition).collect(Collectors.toSet());
        };
    }
}
