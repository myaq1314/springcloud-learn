package com.zz.gateway.dubbo.common.exception;

/**
 * Created by Francis.zz on 2017/7/10.
 */
public class BizException extends RuntimeException {
    public BizException(final Throwable e) {
        super(e);
    }

    public BizException(final String message) {
        super(message);
    }

    public BizException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
