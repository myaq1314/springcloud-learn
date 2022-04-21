package com.zz.gateway.dubbo.core.annotation;

import com.zz.gateway.client.core.annotation.GatewayDubboClient;
import com.zz.gateway.dubbo.core.config.DubboReferenceConfigProperties;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.springframework.boot.WebApplicationType;

import java.io.IOException;

public interface DubboApiWrapperFactory {

	public  Class<?> make(GatewayDubboClient gatewayDubboClient, Class<?> interfaceClass,
                          DubboReferenceConfigProperties dubboReferenceConfigProperties, WebApplicationType webApplicationType)
			throws CannotCompileException, NotFoundException, IllegalArgumentException, IllegalAccessException,
			IOException;
}
