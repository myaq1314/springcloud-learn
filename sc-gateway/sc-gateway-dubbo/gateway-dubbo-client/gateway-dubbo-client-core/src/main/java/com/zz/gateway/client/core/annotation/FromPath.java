package com.zz.gateway.client.core.annotation;

import com.zz.gateway.client.core.annotation.ParamAttribute.ParamFormat;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParamAttribute(paramFromType = ParamFromType.PATH)
public @interface FromPath {
	
	@AliasFor(annotation = ParamAttribute.class)
	String value() default "";

	@AliasFor(annotation = ParamAttribute.class)
	String name() default "";
	
	@AliasFor(annotation = ParamAttribute.class)
	boolean required() default true;
	
	@AliasFor(annotation = ParamAttribute.class)
	ParamFormat paramFormat() default ParamFormat.MAP;
}
