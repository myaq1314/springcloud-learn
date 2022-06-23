package com.zz.gateway.client.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * ************************************
 * create by Intellij IDEA
 * 添加该注解则会将body体整个字符串赋值
 * 区别于不加注解的情况，不加注解是从body体取字段名对应的属性
 *
 * @author Francis.zz
 * @date 2022-04-16 11:07
 * ************************************
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParamAttribute(paramFromType = ParamFromType.BODY)
public @interface FromBody {
	
	@AliasFor(annotation = ParamAttribute.class)
	boolean required() default true;
}
