package com.zz.gateway.dubbo.core.nacos;

/**
 * ************************************
 * create by Intellij IDEA
 * 元数据内容转化
 *
 * @author Francis.zz
 * @date 2022-04-21 15:20
 * ************************************
 */
public interface MetaDataConverter<S, T> {
    T convert(S source);
}
