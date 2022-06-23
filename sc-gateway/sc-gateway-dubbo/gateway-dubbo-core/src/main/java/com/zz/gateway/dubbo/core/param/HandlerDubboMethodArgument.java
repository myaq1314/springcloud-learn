package com.zz.gateway.dubbo.core.param;

import com.zz.gateway.client.core.parse.ParamData;
import javafx.util.Pair;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-06-15 15:18
 * ************************************
 */
public interface HandlerDubboMethodArgument {
    Pair<String[], Object[]> resolve(ServerWebExchange exchange, ParamData[] paramDatas);
}
