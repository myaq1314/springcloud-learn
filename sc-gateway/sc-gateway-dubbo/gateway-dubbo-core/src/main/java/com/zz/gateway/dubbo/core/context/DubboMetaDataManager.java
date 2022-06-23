package com.zz.gateway.dubbo.core.context;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.dubbo.core.handler.DubboMetaDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    /**
     * dubbo接口元数据缓存
     * key：网关请求path
     */
    private static final Map<String, DubboApiMetaData> META_CACHE = Maps.newConcurrentMap();
    private DubboMetaDataHandler<DubboApiMetaData> metaDataHandler;
    private ApplicationEventPublisher eventPublisher;

    public DubboMetaDataManager(DubboMetaDataHandler<DubboApiMetaData> metaDataHandler, ApplicationEventPublisher eventPublisher) {
        this.metaDataHandler = metaDataHandler;
        this.eventPublisher = eventPublisher;
    }

    public static DubboApiMetaData get(String path) {
        DubboApiMetaData metaData = META_CACHE.get(path);

        return metaData;
    }

    public static Map<String, DubboApiMetaData> getCache() {
        return Collections.unmodifiableMap(META_CACHE);
    }

    /**
     * dubbo接口元数据
     * key: {@link DubboApiMetaData#assembleMetaKey()}
     */
    public synchronized void onChange(Map<String, DubboApiMetaData> metaData) {
        if(merge(metaData)) {
            log.info("dubbo api meta data have been updated");
            eventPublisher.publishEvent(new RefreshRoutesEvent(this));
        }
    }

    private boolean merge(Map<String, DubboApiMetaData> newData) {
        if(META_CACHE.size() == 0) {
            newData.forEach((k, v) -> {
                META_CACHE.put(v.getPath(), v);
                metaDataHandler.registry(v.getPath(), v);
            });
            return true;
        }
        Map<String, DubboApiMetaData> oldData = getCache();
        if(oldData.equals(newData)) {
            return false;
        }

        Set<DubboApiMetaData> newMetaData = Sets.newHashSet(newData.values());
        Set<String> clearKeys = new HashSet<>();
        // 不能直接清理 META_CACHE
        oldData.forEach((k, val) -> {
            if(!newMetaData.contains(val)) {
                clearKeys.add(k);
                metaDataHandler.destroy(val.getPath());
            }
        });
        META_CACHE.keySet().removeAll(clearKeys);

        newData.forEach((k, val) -> {
            String path = val.getPath();
            DubboApiMetaData oldMeta;
            if((oldMeta = META_CACHE.put(path, val)) != null) {
                if(!oldMeta.equals(val)) {
                    log.warn("duplicate dubbo api meta. old:" + oldMeta.assembleMetaKey() + " new:" + val.assembleMetaKey());
                    metaDataHandler.destroy(path);
                    metaDataHandler.registry(path, val);
                }
            } else {
                metaDataHandler.registry(path, val);
            }
        });

        return true;
    }
}
