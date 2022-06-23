package com.zz.gateway.dubbo.core.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import com.zz.gateway.dubbo.common.constant.NacosConstants;
import com.zz.gateway.dubbo.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 10:37
 * ************************************
 */
@Slf4j
public class MetaDataNacosListener<T> implements DisposableBean {
    private ConfigService configService;

    private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("dubbo-nacos-meta-update"),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private final MetaDataConverter<String, T> dataConverter;
    private OnChange<T> onChange;
    private static final int DEFAULT_TIMEOUT = 3000;

    public MetaDataNacosListener(NacosProperties config, MetaDataConverter<String, T> dataConverter, OnChange<T> onChange) {
        String serverAddr = config.getServerAddr();
        Properties nacosProperties = new Properties();
        nacosProperties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        if(StringUtils.hasText(config.getNamespace())) {
            nacosProperties.put(PropertyKeyConst.NAMESPACE, config.getNamespace());
        }
        if(StringUtils.hasText(config.getUsername())) {
            // the nacos authentication username
            nacosProperties.put(PropertyKeyConst.USERNAME, config.getUsername());
        }
        if(StringUtils.hasText(config.getPassword())) {
            // the nacos authentication password
            nacosProperties.put(PropertyKeyConst.PASSWORD, config.getPassword());
        }
        if(StringUtils.hasText(config.getAccessKey())) {
            // access key for namespace
            nacosProperties.put(PropertyKeyConst.ACCESS_KEY, config.getAccessKey());
        }
        if(StringUtils.hasText(config.getSecretKey())) {
            // secret key for namespace
            nacosProperties.put(PropertyKeyConst.SECRET_KEY, config.getSecretKey());
        }
        this.dataConverter = dataConverter;
        this.onChange = onChange;

        initNacosListener(nacosProperties);
        loadMetaConfig();
    }

    private void initNacosListener(Properties nacosProperties) {
        try {
            this.configService = ConfigFactory.createConfigService(nacosProperties);
            this.configService.addListener(NacosConstants.META_DATA_ID, NacosConstants.DEFAULT_GROUP, new Listener() {
                @Override
                public Executor getExecutor() {
                    return pool;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    MetaDataNacosListener.this.registerMetaData(configInfo);
                }
            });
        } catch (NacosException e) {
            throw new BizException("Nacos config service initialized error occurred", e);
        }
    }

    private void loadMetaConfig() {
        try {
            if (configService == null) {
                throw new IllegalStateException("Nacos config service has not been initialized");
            }
            String config = configService.getConfig(NacosConstants.META_DATA_ID, NacosConstants.DEFAULT_GROUP, DEFAULT_TIMEOUT);
            if (config == null) {
                log.warn("init dubbo meta data is null, please check nacos config server");
                return;
            }
            this.registerMetaData(config);
        } catch (Exception ex) {
            log.warn("Error when loading initial dubbo meta config", ex);
        }
    }

    public void registerMetaData(String metaConfig) {
        T metaData = this.dataConverter.convert(metaConfig);
        onChange.change(metaData);
    }

    @Override
    public void destroy() throws Exception {
        try {
            if(configService != null) {
                configService.shutDown();
            }
            pool.shutdownNow();
        } catch (Exception e) {
            log.error("nacos connect close failed");
        }
    }

    public interface OnChange<T> {
        void change(T changeData);
    }
}
