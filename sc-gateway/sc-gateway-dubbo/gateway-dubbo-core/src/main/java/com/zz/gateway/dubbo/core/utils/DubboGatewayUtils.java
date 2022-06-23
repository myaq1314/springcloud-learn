package com.zz.gateway.dubbo.core.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 16:30
 * ************************************
 */
public class DubboGatewayUtils {
    public static boolean isSimpleType(String type) {
        final List<String> baseTypes = Lists.newArrayList(
                "java.lang.Integer",
                "java.lang.Double",
                "java.lang.Float",
                "java.lang.Long",
                "java.lang.Short",
                "java.lang.Byte",
                "java.lang.Boolean",
                "java.lang.Character",
                "java.lang.String",
                "int","double","long","short","byte","boolean","char","float"
        );

        return (!"java.lang.Void".equals(type) && baseTypes.contains(type)) ||
                type.startsWith("[L");
    }

    public static boolean isMapType(String type) {
        if(StringUtils.isEmpty(type)) {
            return false;
        }

        return "java.util.Map".equals(type) ||
                "java.util.HashMap".equals(type) ||
                "java.util.LinkedHashMap".equals(type);
    }

    public static boolean isJsonObject(String jsonStr) {
        return jsonStr.startsWith("{") && jsonStr.endsWith("}");
    }

    public static boolean isJsonArray(String jsonStr) {
        return jsonStr.startsWith("[") && jsonStr.endsWith("]");
    }
}
