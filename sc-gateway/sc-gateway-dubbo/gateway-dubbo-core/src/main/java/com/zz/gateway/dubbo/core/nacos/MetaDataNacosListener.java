package com.zz.gateway.dubbo.core.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zz.gateway.dubbo.common.config.NacosProperties;
import com.zz.gateway.dubbo.common.exception.BizException;
import com.zz.gateway.dubbo.core.handler.DubboMetaDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-21 10:37
 * ************************************
 */
@Slf4j
public class MetaDataNacosListener implements DisposableBean {
    private ConfigService configService;
    private DubboMetaDataHandler metaDataHandler;

    public MetaDataNacosListener(NacosProperties config, DubboMetaDataHandler metaDataHandler) {
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
        this.metaDataHandler = metaDataHandler;
    }

    public void listener(final String dataId, final OnChange oc) {
        Listener listener = new Listener() {
            @Override
            public void receiveConfigInfo(final String configInfo) {
                oc.change(configInfo);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        };
        oc.change(getConfigAndSignListener(dataId, listener));
        LISTENERS.computeIfAbsent(dataId, key -> new ArrayList<>()).add(listener);
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
}
