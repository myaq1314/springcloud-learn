package com.zz.scservice.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-18 14:52
 * ************************************
 */
@Component
@Slf4j
public class OrderClientFactory implements FallbackFactory<OrderClientFallback> {
    @Override
    public OrderClientFallback create(Throwable cause) {
        return new OrderClientFallback(cause);
    }
}
