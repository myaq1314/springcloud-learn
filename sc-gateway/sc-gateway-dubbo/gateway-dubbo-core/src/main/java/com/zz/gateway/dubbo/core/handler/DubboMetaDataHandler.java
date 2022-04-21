package com.zz.gateway.dubbo.core.handler;

import com.zz.gateway.dubbo.common.protocol.DubboApiMetaData;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 10:24
 * ************************************
 */
public interface DubboMetaDataHandler {
    void doHandler(DubboApiMetaData metaData);
}
