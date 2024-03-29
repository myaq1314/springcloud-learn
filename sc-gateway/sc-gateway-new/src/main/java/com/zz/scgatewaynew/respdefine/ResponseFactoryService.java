package com.zz.scgatewaynew.respdefine;

import com.zz.gateway.common.GatewayConstants;
import com.zz.gateway.common.util.GatewayUtil;
import com.zz.scgatewaynew.config.SettingProp;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-17 16:53
 * ************************************
 */
@Component
public class ResponseFactoryService {
    @Autowired
    private SettingProp settingProp;
    
    public UpstreamResponse getRespStrategy(int strategy) {
        if(GatewayConstants.SP_RESP_STRATEGY == strategy) {
            return new SPApiResponse(settingProp.getPrivateKeyStr());
        } else if(GatewayConstants.ORDER_RESP_STRATEGY == strategy) {
            return new OrderApiResponse();
        } else if(GatewayConstants.WECHAT_RESP_STRATEGY == strategy) {
            return new WechatResponse();
        }
        
        return new SPApiResponse(settingProp.getPrivateKeyStr());
    }
    
    /**
     * 失败响应信息，如有需要这里可以设置content-type
     *
     * @param exchange
     * @param msg
     * @param code
     * @return
     */
    public UpstreamResponse.Response failResponseInfo(ServerWebExchange exchange, String msg, String code) {
        String strObj = GatewayUtil.routeMetadata(exchange, GatewayConstants.META_RESP_FORMAT);
        UpstreamResponse response;
        if(strObj == null) {
            response = new NotFoundResponse();
        } else {
            int strategy = NumberUtils.toInt(strObj, GatewayConstants.SP_RESP_STRATEGY);
            response = getRespStrategy(strategy);
        }
        
        return response.failResp(code, msg, exchange);
    }
    
    /**
     * 检查上游服务响应数据是否异常(业务响应判断)
     *
     * @param exchange
     * @return
     */
    public boolean isFailResponse(ServerWebExchange exchange, String respBody) {
        String strObj = GatewayUtil.routeMetadata(exchange, GatewayConstants.META_RESP_FORMAT);
        if(strObj == null) {
            return false;
        }
        int strategy = NumberUtils.toInt(strObj + "", GatewayConstants.SP_RESP_STRATEGY);
        return !getRespStrategy(strategy).isSuccessResponse(respBody);
    }
}
