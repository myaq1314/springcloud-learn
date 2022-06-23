package com.zz.gateway.client.core.parse;

import com.zz.gateway.client.core.annotation.ParamAttribute;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 18:03
 * ************************************
 */
public class MetaDataParseUtil {
    public static ParamData[] buildParamMetaData(Method method) {
        return MetaDataParseUtil.buildParamMetaData(method.getParameters());
    }

    public static ParamData[] buildParamMetaData(Parameter[] parameters) {
        if(parameters == null || parameters.length == 0) {
            return null;
        }
        ParamData[] pdInfos = new ParamData[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter cur = parameters[i];
            // getName 和 getTypeName 对于数组格式有区别有区别
            // getName: [Ljava.lang.String;
            // getTypeName: java.lang.String[]
            String typeName = cur.getType().getName();
            if(Map.class.isAssignableFrom(cur.getType())) {
                typeName = Map.class.getName();
            }
            ParamData paramData = new ParamData(cur.getName(), typeName, i + 1, cur.getDeclaringExecutable().getName());
            ParamAttribute an = AnnotatedElementUtils.getMergedAnnotation(cur, ParamAttribute.class);
            if(an != null) {
                paramData.setFromWhere(an.paramFromType().name());
                if(StringUtils.hasText(an.name())) {
                    paramData.setParamName(an.name());
                }
                paramData.setRequired(an.required());
            }
            // 数组存放的属性是基础类型也算基础类型，List不是基础类型
            paramData.setSimpleType(BeanUtils.isSimpleProperty(cur.getType()));

            pdInfos[i] = paramData;
        }

        return pdInfos;
    }
}
