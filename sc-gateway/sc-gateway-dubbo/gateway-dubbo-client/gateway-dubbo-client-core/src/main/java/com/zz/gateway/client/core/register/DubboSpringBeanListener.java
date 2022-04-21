package com.zz.gateway.client.core.register;

import com.zz.gateway.client.core.annotation.GatewayDubboClient;
import com.zz.gateway.client.core.common.GatewayDubboClientConfig;
import com.zz.gateway.dubbo.common.protocol.DubboApiMetaData;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-04-12 14:34
 * ************************************
 */
public class DubboSpringBeanListener implements ApplicationListener<ContextRefreshedEvent> {
    private NacosClientRegisterRepository clientRegisterRepository;
    private final AtomicBoolean registered = new AtomicBoolean(false);

    private final String appName;

    public DubboSpringBeanListener(final GatewayDubboClientConfig clientConfig,
                                   final NacosClientRegisterRepository nacosClientRegisterRepository) {
        this.appName = clientConfig.getAppName();
        this.clientRegisterRepository = nacosClientRegisterRepository;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        Map<String, ServiceBean> serviceBean = contextRefreshedEvent.getApplicationContext().getBeansOfType(ServiceBean.class);
        for (Map.Entry<String, ServiceBean> entry : serviceBean.entrySet()) {
            register(entry.getValue());
        }
        clientRegisterRepository.publishMeta(this.appName);
    }

    private void register(final ServiceBean serviceBean) {
        Object refProxy = serviceBean.getRef();
        Class<?> clazz = refProxy.getClass();
        if (AopUtils.isAopProxy(refProxy)) {
            clazz = AopUtils.getTargetClass(refProxy);
        }
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(clazz);
        for (Method method : methods) {
            GatewayDubboClient gatewayDubboClient = method.getAnnotation(GatewayDubboClient.class);
            if (Objects.nonNull(gatewayDubboClient)) {
                clientRegisterRepository.registerConfig(buildMetaDataDTO(serviceBean, gatewayDubboClient, method));
            }
        }
    }

    private DubboApiMetaData buildMetaDataDTO(final ServiceBean serviceBean, final GatewayDubboClient gatewayDubboClient,
                                              final Method method) {
        String appName = buildAppName(serviceBean);
        String path = gatewayDubboClient.path();
        String desc = gatewayDubboClient.desc();
        String serviceName = serviceBean.getInterface();
        String methodName = method.getName();
        Class<?>[] parameterTypesClazz = method.getParameterTypes();
        String parameterTypes = Arrays.stream(parameterTypesClazz).map(Class::getName).collect(Collectors.joining(","));
        return DubboApiMetaData.builder()
                .appName(appName)
                .interfaceName(serviceName)
                .methodName(methodName)
                .path(path)
                .pathDesc(desc)
                .parameterTypes(parameterTypes)
                .version(StringUtils.isNotEmpty(serviceBean.getVersion()) ? serviceBean.getVersion() : "")
                .group(StringUtils.isNotEmpty(serviceBean.getGroup()) ? serviceBean.getGroup() : "")
                .loadBalance(StringUtils.isNotEmpty(serviceBean.getLoadbalance()) ? serviceBean.getLoadbalance() : CommonConstants.DEFAULT_LOADBALANCE)
                .retries(Objects.isNull(serviceBean.getRetries()) ? CommonConstants.DEFAULT_RETRIES : serviceBean.getRetries())
                .timeout(Objects.isNull(serviceBean.getTimeout()) ? CommonConstants.DEFAULT_TIMEOUT : serviceBean.getTimeout())
                .url("")
                .enabled(gatewayDubboClient.enabled())
                .build();
    }

    private String buildAppName(final ServiceBean serviceBean) {
        return StringUtils.isBlank(this.appName) ? serviceBean.getApplication().getName() : this.appName;
    }
}

