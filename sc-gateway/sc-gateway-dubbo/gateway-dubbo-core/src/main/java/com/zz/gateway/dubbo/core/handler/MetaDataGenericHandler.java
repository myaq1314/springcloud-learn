package com.zz.gateway.dubbo.core.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-22 17:48
 * ************************************
 */
@Slf4j
public class MetaDataGenericHandler implements DubboMetaDataHandler<DubboApiMetaData> {
    private static final Map<String, ReferenceConfig<GenericService>> GENERIC_SERVICE_CACHE = Maps.newConcurrentMap();

    private static Map<String, ReferenceConfig<GenericService>> getCache() {
        return Collections.unmodifiableMap(GENERIC_SERVICE_CACHE);
    }

    @Override
    public void registryAll(final Map<String, DubboApiMetaData> metaData) {
        metaData.forEach((k, v) -> this.registry(v.getPath(), v));
    }

    @Override
    public void registry(String path, DubboApiMetaData metaData) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setGeneric(CommonConstants.GENERIC_SERIALIZATION_DEFAULT);
        // TODO 参数设置是否异步调用
        reference.setAsync(true);
        reference.setInterface(metaData.getInterfaceName());
        reference.setProtocol("dubbo");
        reference.setLoadbalance(StringUtils.hasText(metaData.getLoadBalance()) ? metaData.getLoadBalance() : "gray");
        reference.setVersion(metaData.getVersion());
        reference.setGroup(metaData.getGroup());
        reference.setUrl(metaData.getUrl());
        // TODO 设置默认超时时间
        reference.setTimeout(metaData.getTimeout() == null ? 2000 : metaData.getTimeout());
        reference.setRetries(metaData.getRetries());
        reference.setCheck(false);

        // 这里如果引入的是dubbo-spring-boot-starter，就不需要配置RegistryConfig和ApplicationConfig
//            RegistryConfig registryConfig = new RegistryConfig();
//            registryConfig.setProtocol("dubbo");
//            registryConfig.setId("gateway");
//            registryConfig.setAddress("nacos://172.16.80.153:8848?namespace=9820bd7a-1df1-4f20-96fd-a8427bd92fdd");
//            reference.setRegistry(registryConfig);

        try {
            Object obj = reference.get();
            if (obj != null) {
                log.info("init apache dubbo reference success. there meteData is :{}", metaData);
                GENERIC_SERVICE_CACHE.put(path, reference);
            }
        } catch (Exception e) {
            log.error("init apache dubbo reference exception, metaData:" + JSON.toJSONString(metaData), e);
        }
    }

    @Override
    public void destroy(String path) {
        ReferenceConfig<GenericService> reference = GENERIC_SERVICE_CACHE.get(path);
        if(reference == null) {
            return;
        }
        try {
            reference.destroy();
        } catch (Exception e) {
            // noop
            log.info("dubbo gateway consumer destroy fail. path:" + path);
        }
        GENERIC_SERVICE_CACHE.remove(path);
    }

    public static ReferenceConfig<GenericService> get(String path) {
        return GENERIC_SERVICE_CACHE.get(path);
    }
}
