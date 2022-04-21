package com.zz.gateway.dubbo.common.utils;

import com.alibaba.fastjson.JSON;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-18 11:00
 * ************************************
 */
public class JsonParseUtil {
    public static String toJson(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz);
    }
}
