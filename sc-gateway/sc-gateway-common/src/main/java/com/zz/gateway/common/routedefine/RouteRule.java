package com.zz.gateway.common.routedefine;

import com.zz.gateway.common.GatewayConstants;
import com.zz.gateway.common.routedefine.filterrule.FilterGroup;
import com.zz.gateway.common.routedefine.predicaterule.PredicateGroup;
import com.zz.sccommon.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-28 11:24
 * ************************************
 */
@Data
@Slf4j
public class RouteRule implements RuleCheck {
    private String id;
    private PredicateGroup predicate;
    private FilterGroup filter;
    /**
     * 访问地址
     * eg： http://ip:port/contextPath
     */
    private String uri;
    /**
     * 路由规则优先级，值越小优先级越高
     */
    private int order;
    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    public void getRoute(RouteLocatorBuilder.Builder builder) {
        //this.validate(commonPredicate);
    
        if(predicate == null) {
            throw new IllegalArgumentException("args[predicate] must be not null");
        }
        builder.route(id, p -> {
            BooleanSpec uriSpec;
            uriSpec = predicate.predicate(p.order(order));
            if(filter != null) {
                uriSpec.filters(f -> filter.filter(f));
            }
            if(metadata != null) {
                uriSpec.metadata(getMetadata());
            }
            return uriSpec.uri(URI.create(uri));
        });
    }
    
    /**
     * 组装路由规则
     *
     * @param builder
     * @param commonPredicate 共用的predicate
     */
    public void getRouteWithCommon(RouteLocatorBuilder.Builder builder, Function<PredicateSpec, BooleanSpec> commonPredicate) {
        if(predicate != null) {
            builder.route(id, p -> {
                BooleanSpec uriSpec;
                if(commonPredicate != null) {
                    uriSpec = commonPredicate.apply(p);
                    p = uriSpec.and();
                }
                uriSpec = predicate.predicate(p.order(order));
                if(filter != null) {
                    uriSpec.filters(f -> filter.filter(f));
                }
                
                return uriSpec.uri(URI.create(uri));
            });
        }
    }
    
    public void validatePlus() {
        if(id == null || uri == null) {
            throw new IllegalArgumentException("[id, uri] must be not null");
        }
    
        if(predicate == null) {
            throw new IllegalArgumentException("args[predicate] must be not null");
        }
        
        this.validate();
    }
    
    public boolean isValid() {
        try {
            this.validatePlus();
        } catch (IllegalArgumentException e) {
            log.error("route rule[" + this.id + "] is invalid, " + e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("route rule[" + this.id + "] is invalid, " + e);
            return false;
        }
        
        return true;
    }

    public Map<String, Object> getMetadata() {
        if(metadata == null) {
            return null;
        }

        if(metadata.containsKey(GatewayConstants.META_EXT_PARAMS)) {
            String extParams = (String) metadata.get(GatewayConstants.META_EXT_PARAMS);
            Map<String, String> extMap = new HashMap<>();
            JsonUtils.parseJson2Map(extMap, extParams);
            metadata.putAll(extMap);
        }
        return Collections.unmodifiableMap(metadata);
    }
}
