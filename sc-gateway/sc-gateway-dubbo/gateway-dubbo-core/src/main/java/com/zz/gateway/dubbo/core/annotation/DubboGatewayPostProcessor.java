package com.zz.gateway.dubbo.core.annotation;

import com.zz.gateway.dubbo.core.context.DubboApiContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.PathMatcher;

import java.util.Map;

public class DubboGatewayPostProcessor implements SmartInitializingSingleton, ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	@Autowired
	private PathMatcher pathMatcher = null;

	@Override
	public void afterSingletonsInstantiated() {
		Map<String, DubboApiWrapper> mapDubboApiWrapper = applicationContext.getBeansOfType(DubboApiWrapper.class);
		for (Map.Entry<String, DubboApiWrapper> entry : mapDubboApiWrapper.entrySet()) {
			for (String pathPattern : entry.getValue().getPathPatterns()) {
			    // path路径是否正则表达式
				if (pathMatcher.isPattern(pathPattern)) {
					DubboApiContext.MAP_DUBBO_API_PATH_PATTERN_WRAPPER.put(pathPattern, entry.getValue());
				} else {
					DubboApiContext.MAP_DUBBO_API_WRAPPER.put(pathPattern, entry.getValue());
				}
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
