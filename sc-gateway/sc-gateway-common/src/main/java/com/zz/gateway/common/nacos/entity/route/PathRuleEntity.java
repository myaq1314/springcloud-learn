package com.zz.gateway.common.nacos.entity.route;

import com.zz.gateway.common.routedefine.RuleCheck;
import lombok.Data;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-03-26 17:01
 * ************************************
 */
@Data
public class PathRuleEntity implements RuleCheck {
    private String[] path;
    
    /**
     * predicate顺序，值越小越优先. path应该优先级比其他predicate高，这样才能使respStrategy参数生效
     */
    private Integer order;
    
    public PathRuleEntity(String text) {
        this.path = text.split(",");
    }
    
    public PathRuleEntity() {
    }
}
