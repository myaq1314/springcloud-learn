package com.zz.gateway.client.core.register;

import com.zz.gateway.client.core.annotation.GatewayDubboClient;
import com.zz.gateway.client.core.annotation.RequestMethod;
import com.zz.gateway.client.core.common.GatewayDubboClientConfig;
import com.zz.gateway.client.core.parse.DubboApiMetaData;
import com.zz.gateway.client.core.parse.MetaDataParseUtil;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
            // 因为AliasFor是spring提供的，因此这里要使用spring的方法获取注解
            GatewayDubboClient gatewayDubboClient = AnnotatedElementUtils.getMergedAnnotation(method, GatewayDubboClient.class);
            if (Objects.nonNull(gatewayDubboClient)) {
                clientRegisterRepository.registerConfig(buildMetaDataDTO(serviceBean, gatewayDubboClient, method));
            }
        }
    }

    private DubboApiMetaData buildMetaDataDTO(final ServiceBean serviceBean, final GatewayDubboClient gatewayDubboClient,
                                              final Method method) {
        String appName = buildAppName(serviceBean);
        // todo 不能拿到value的值，只能拿到path的
        String path = gatewayDubboClient.path();
        String desc = gatewayDubboClient.desc();

        String serviceName = serviceBean.getInterface();
        String methodName = method.getName();
        return DubboApiMetaData.builder()
                .appName(appName)
                .interfaceName(serviceName)
                .methodName(methodName)
                .path(path)
                .requestMethod(buildRequestMethod(gatewayDubboClient.requestMethod()))
                .pathDesc(desc)
                .params(MetaDataParseUtil.buildParamMetaData(method))
                .version(StringUtils.isNotEmpty(serviceBean.getVersion()) ? serviceBean.getVersion() : "")
                .group(StringUtils.isNotEmpty(serviceBean.getGroup()) ? serviceBean.getGroup() : "")
                .loadBalance(StringUtils.isNotEmpty(serviceBean.getLoadbalance()) ? serviceBean.getLoadbalance() : CommonConstants.DEFAULT_LOADBALANCE)
                .retries(Objects.isNull(serviceBean.getRetries()) ? CommonConstants.DEFAULT_RETRIES : serviceBean.getRetries())
                .timeout(Objects.isNull(serviceBean.getTimeout()) ? CommonConstants.DEFAULT_TIMEOUT : serviceBean.getTimeout())
                .url("")
                .enabled(gatewayDubboClient.enabled())
                .build();
    }

    private String buildRequestMethod(final RequestMethod[] requestMethods) {
        if(requestMethods == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < requestMethods.length; i++) {
            if(i != 0) {
                sb.append(",");
            }
            sb.append(requestMethods[i].name());
        }
        return sb.toString();
    }

    private String buildAppName(final ServiceBean serviceBean) {
        // TODO appName不配置会空指针，需要从别处获取
        return StringUtils.isBlank(this.appName) ? serviceBean.getApplication().getName() : this.appName;
    }
}

