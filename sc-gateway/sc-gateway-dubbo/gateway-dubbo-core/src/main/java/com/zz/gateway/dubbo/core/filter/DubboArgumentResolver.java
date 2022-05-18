package com.zz.gateway.dubbo.core.filter;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-05-06 14:36
 * ************************************
 */
public class DubboArgumentResolver {
    Decoder<Object> decoder = new Jackson2JsonDecoder();

     /**
     * todo 还缺少多参数的逻辑和自定义取值的问题
     */
    public Object[] resolve(final ServerWebExchange exchange, final DataBuffer body) {
        Map<String, Object> payload = (Map<String, Object>) decoder.decode(body, ResolvableType.forType(Map.class),
                MimeTypeUtils.APPLICATION_JSON, null);
        // TODO
        DataBufferUtils.release(body);
        //return payload.values().toArray(new Object[]{});
        return new Object[]{payload};
    }
}
