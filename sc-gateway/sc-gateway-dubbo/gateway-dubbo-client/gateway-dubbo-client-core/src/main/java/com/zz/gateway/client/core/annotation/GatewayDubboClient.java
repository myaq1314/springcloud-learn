package com.zz.gateway.client.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GatewayDubboClient {
    /**
     * 用来指定 {@link com.zz.gateway.support.config.DubboReferenceConfigProperties#dubboRefer}对应的参数
     * 分组，网关通过该分组设置不同的参数，@DubboReference中的参数
     * @return
     */
	String id() default "";

    @AliasFor("path")
    String value() default "";

    @AliasFor("value")
    String path() default "";

    String desc() default "";

    /**
     * 是否对网关开放接口
     */
    boolean enabled() default true;

    RequestMethod[] requestMethod() default {RequestMethod.POST};
}
