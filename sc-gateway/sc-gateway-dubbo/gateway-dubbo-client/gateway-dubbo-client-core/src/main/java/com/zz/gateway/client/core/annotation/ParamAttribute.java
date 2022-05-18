package com.zz.gateway.client.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamAttribute {

	@AliasFor("name")
	String value() default "";

	@AliasFor("value")
	String name() default "";
	
	boolean required() default true;
	
	ParamFromType paramFromType();
	
	ParamFormat paramFormat() default ParamFormat.MAP;
	
	enum ParamFormat {
		MAP, JSON
	}
}
