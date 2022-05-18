package com.zz.gateway.dubbo.core.context;

import com.google.common.collect.Maps;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.dubbo.core.handler.DubboMetaDataHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 14:37
 * ************************************
 */
@Slf4j
public class DubboMetaDataManager {
    private static final Map<String, DubboApiMetaData> META_CACHE = Maps.newConcurrentMap();
    private DubboMetaDataHandler<Map<String, DubboApiMetaData>> metaDataHandler;

    public DubboMetaDataManager(DubboMetaDataHandler<Map<String, DubboApiMetaData>> metaDataHandler) {
        this.metaDataHandler = metaDataHandler;
    }

    public static DubboApiMetaData get(String path) {
        DubboApiMetaData metaData = META_CACHE.get(path);

        return metaData;
    }

    public void onChange(Map<String, DubboApiMetaData> metaData) {
        metaData.forEach((key, meta) -> {
            String path = meta.getPath();
            if(META_CACHE.putIfAbsent(path, meta) != null) {
                META_CACHE.put(key, meta);
            }
        });

        System.out.println("meta keys:" + META_CACHE.keySet());
        log.info("dubbo api meta data have been updated");
        metaDataHandler.doHandler(META_CACHE);
    }
}
