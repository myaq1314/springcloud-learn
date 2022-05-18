package com.zz.gateway.client.core.parse;

import com.zz.gateway.client.core.annotation.ParamFromType;
import lombok.Data;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-22 15:22
 * ************************************
 */
@Data
public class ParamData {
    /**
     * 参数名
     */
    private String paramName;
    /**
     * 参数类型
     */
    private String paramType;
    /**
     * 参数来源
     */
    private ParamFromType fromWhere;
    /**
     * 是否必须
     */
    private boolean required;
    /**
     * 是否是普通类型
     */
    private boolean simpleType;
    /**
     * 参数位置，从1开始
     */
    private int paramIndex;

    public ParamData() {
    }

    public ParamData(String paramName, String paramType, int paramIndex) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramIndex = paramIndex;
    }
}
