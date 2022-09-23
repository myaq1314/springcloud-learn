package com.zz.scorder.config;

import com.alibaba.cloud.sentinel.feign.SentinelContractHolder;
import feign.MethodMetadata;
import feign.Target;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-12 14:34
 * ************************************
 */
public class FeignSpringBeanListener implements ApplicationListener<ContextRefreshedEvent> {
    private final AtomicBoolean registered = new AtomicBoolean(false);

    private final String appName;

    public FeignSpringBeanListener() {
        this.appName = "clientConfig.getAppName()";
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        // Map<String, Object> serviceBean = contextRefreshedEvent.getApplicationContext().getBeansWithAnnotation(FeignClient.class);
        if(SentinelContractHolder.METADATA_MAP.size() == 0) {
            return;
        }
        Map<String, MethodMetadata> metaMap = SentinelContractHolder.METADATA_MAP;
        if (!registered.compareAndSet(false, true)) {
            return;
        }
    }
}

