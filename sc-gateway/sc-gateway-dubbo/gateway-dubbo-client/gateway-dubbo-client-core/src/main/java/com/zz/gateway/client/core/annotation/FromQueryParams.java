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
@ParamAttribute(paramFromType = ParamAttribute.ParamFromType.FROM_QUERYPARAMS)
public @interface FromQueryParams {
	
	@AliasFor(annotation = ParamAttribute.class)
	String value() default "";

	@AliasFor(annotation = ParamAttribute.class)
	String name() default "";
	
	@AliasFor(annotation = ParamAttribute.class)
	boolean required() default true;
	
	@AliasFor(annotation = ParamAttribute.class)
    ParamAttribute.ParamFormat paramFormat() default ParamAttribute.ParamFormat.MAP;
}
