package com.zz.gateway.dubbo.core.handler;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 10:24
 * ************************************
 */
public interface DubboMetaDataHandler<T> {
    default void registryAll(Map<String, T> metaData) {

    }

    default void registry(String path, T metaData) {

    }

    default void destroy(String path) {

    }
}
