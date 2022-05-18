package com.zz.gateway.client.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParamAttribute(paramFromType = ParamFromType.BODY)
public @interface FromBody {
	
	@AliasFor(annotation = ParamAttribute.class)
	boolean required() default true;
}
