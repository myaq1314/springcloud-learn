package com.zz.gateway.client.core.parse;

import com.zz.gateway.client.core.annotation.ParamAttribute;
import org.apache.dubbo.common.utils.ClassUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
            ParamData paramData = new ParamData(cur.getName(), cur.getType().getName(), i + 1);
            ParamAttribute an = AnnotatedElementUtils.getMergedAnnotation(cur, ParamAttribute.class);
            if(an != null) {
                paramData.setFromWhere(an.paramFromType());
                if(!StringUtils.isEmpty(an.name())) {
                    paramData.setParamName(an.name());
                }
                paramData.setRequired(an.required());
            }
            paramData.setSimpleType(ClassUtils.isSimpleType(cur.getType()));

            pdInfos[i] = paramData;
        }

        return pdInfos;
    }
}
