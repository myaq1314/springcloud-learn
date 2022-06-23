package com.zz.gateway.client.core.parse;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.Objects;

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
    private String fromWhere;
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

    /**
     * 所属方法名
     */
    private String methodName;

    public ParamData() {
    }

    public ParamData(String paramName, String paramType, int paramIndex) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramIndex = paramIndex;
    }

    public ParamData(String paramName, String paramType, int paramIndex, String methodName) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramIndex = paramIndex;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ParamData paramData = (ParamData) other;
        return required == paramData.required && paramIndex == paramData.paramIndex &&
                Objects.equals(paramName, paramData.paramName) &&
                Objects.equals(paramType, paramData.paramType) &&
                Objects.equals(methodName, paramData.methodName) &&
                Objects.equals(fromWhere, paramData.fromWhere);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType, fromWhere, methodName) + this.paramIndex;
    }

    @Override
    public String toString() {
        return "ParamData{" +
                "methodName='" + methodName + '\'' +
                ", paramName='" + paramName + '\'' +
                ", paramType='" + paramType + '\'' +
                ", paramIndex=" + paramIndex +
                ", fromWhere=" + fromWhere +
                '}';
    }
}
