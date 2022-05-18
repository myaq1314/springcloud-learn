package com.zz.gateway.dubbo.core.handler;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 10:24
 * ************************************
 */
public interface DubboMetaDataHandler<T> {
    void doHandler(T metaData);
}
