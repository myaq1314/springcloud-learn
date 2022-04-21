package com.zz.gateway.client.core.register;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.collect.Maps;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import com.zz.gateway.dubbo.common.constant.NacosConstants;
import com.zz.gateway.dubbo.common.exception.BizException;
import com.zz.gateway.dubbo.common.protocol.DubboApiMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-15 17:23
 * ************************************
 */
@Slf4j
public class NacosClientRegisterRepository implements DisposableBean {
    private ConfigService configService;
    /**
     * 当前应用的dubbo api元数据集合
     */
    private final ConcurrentMap<String, DubboApiMetaData> metadataCache = Maps.newConcurrentMap();
    /**
     * 仓库中元数据集合
     * key: {@link DubboApiMetaData#assembleMetaKey()}
     */
    private final ConcurrentMap<String, DubboApiMetaData> META_DATA_MAP = Maps.newConcurrentMap();
    private volatile boolean isPublish = false;
    private volatile int metaDataSize;

    public NacosClientRegisterRepository(NacosProperties config) {
        String serverAddr = config.getServerAddr();
        Properties nacosProperties = new Properties();
        nacosProperties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        if(!StringUtils.isEmpty(config.getNamespace())) {
            nacosProperties.put(PropertyKeyConst.NAMESPACE, config.getNamespace());
        }
        if(!StringUtils.isEmpty(config.getUsername())) {
            // the nacos authentication username
            nacosProperties.put(PropertyKeyConst.USERNAME, config.getUsername());
        }
        if(!StringUtils.isEmpty(config.getPassword())) {
            // the nacos authentication password
            nacosProperties.put(PropertyKeyConst.PASSWORD, config.getPassword());
        }
        if(!StringUtils.isEmpty(config.getAccessKey())) {
            // access key for namespace
            nacosProperties.put(PropertyKeyConst.ACCESS_KEY, config.getAccessKey());
        }
        if(!StringUtils.isEmpty(config.getSecretKey())) {
            // secret key for namespace
            nacosProperties.put(PropertyKeyConst.SECRET_KEY, config.getSecretKey());
        }

        try {
            this.configService = ConfigFactory.createConfigService(nacosProperties);
        } catch (NacosException e) {
            throw new BizException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        try {
            if(configService != null) {
                configService.shutDown();
            }
        } catch (Exception e) {
            log.error("nacos connect close failed");
        }
    }

    public synchronized void registerConfig(final DubboApiMetaData metadata) {
        String key = metadata.assembleMetaKey();

        if(metadataCache.put(key, metadata) != null) {
            log.warn("exist duplicate dubbo api. path:" + metadata.getPath() + ", version:" + metadata.getVersion());
        }
    }

    public synchronized void publishMeta(final String appName) {
        if(this.isPublish && this.metaDataSize == metadataCache.size()) {
            return;
        }
        String configName = NacosConstants.META_DATA_ID;
        mergeMetaData(getConfig(configName), appName);
        try {
            final String defaultGroup = NacosConstants.DEFAULT_GROUP;
            configService.publishConfig(configName, defaultGroup, JSON.toJSONString(META_DATA_MAP));
        } catch (NacosException e) {
            log.error("publish meta data fail. meta:" + JSON.toJSONString(META_DATA_MAP));
        }
        log.info("register metadata success, meta size: {}", metadataCache.size());

        this.isPublish = true;
        this.metaDataSize = metadataCache.size();
    }

    private String getConfig(final String dataId) {
        try {
            String config = configService.getConfig(dataId, NacosConstants.DEFAULT_GROUP, NacosConstants.NACOS_TIMED_OUT);
            return StringUtils.hasLength(config) ? config : NacosConstants.DEFAULT_NULL_JSON_VALUE;
        } catch (NacosException e) {
            log.info("get data from nacos error", e);
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 与仓库中meta元数据合并
     *
     * @param configInfo 仓库中的数据
     */
    private void mergeMetaData(final String configInfo, final String appName) {
        Map<String, DubboApiMetaData> oldData = JSON.parseObject(configInfo, new TypeReference<Map<String, DubboApiMetaData>>(){});
        META_DATA_MAP.putAll(oldData);

        Set<String> set = new HashSet<>(metadataCache.keySet());
        Set<String> cancelApi = new HashSet<>();
        for (Map.Entry<String, DubboApiMetaData> e : oldData.entrySet()) {
            META_DATA_MAP.put(e.getKey(), e.getValue());
            if(appName.equals(e.getValue().getAppName()) && !set.contains(e.getKey())) {
                cancelApi.add(e.getKey());
            }
        }
        META_DATA_MAP.putAll(metadataCache);
        // 删除同一个应用中不存在的配置
        META_DATA_MAP.keySet().removeAll(cancelApi);
    }
}
