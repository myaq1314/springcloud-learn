package com.zz.gateway.dubbo.core.autoconfig;

import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

public class DubboGateWayApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	
	private static AtomicBoolean atomicb=new AtomicBoolean(false);
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		if (atomicb.compareAndSet(false, true)) {
			AnnotationConfigApplicationContext annotationConfigApplicationContext = createContext(applicationContext);
			DubboReferenceConfigProperties dubboReferenceConfigProperties = annotationConfigApplicationContext
					.getBean(DubboReferenceConfigProperties.class);
			applicationContext.getBeanFactory().registerSingleton(
					DubboReferenceConfigProperties.class.getName(), dubboReferenceConfigProperties);
		}
	}

	protected AnnotationConfigApplicationContext createContext(
			ConfigurableApplicationContext parentApplicationContext) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setEnvironment(parentApplicationContext.getEnvironment());
		context.setClassLoader(parentApplicationContext.getClassLoader());
		context.register(DubboGatewayBootstrapConfiguration.class);
		context.refresh();
		return context;
	}
}
