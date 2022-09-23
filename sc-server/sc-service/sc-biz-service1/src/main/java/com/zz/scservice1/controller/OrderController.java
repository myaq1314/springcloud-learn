package com.zz.scservice1.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.api.common.protocal.CityCodeConstant;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.feignapi.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-04-23 14:30
 * ************************************
 */
@RestController
@Slf4j
//@RequestMapping(CityCodeConstant.GUANGXI)
public class OrderController implements OrderClient {
    /**
     * openfeign是服务调用的客户端，因此服务提供者这端不需要集成openfeign组件，
     * 也就是说这里是可以不用实现 OrderClient 接口的，实现接口的目的是为了保证请求地址一致，
     * openfeign客户端配置的请求的地址与服务提供者接口对外开放的地址一致即可调用。
     */
    @Value("${server.port}")
    private String port;
    @Value("${spring.application.name}")
    private String serverName;
    
    @Override
    public ApiResponse<String> getOrderInfo(int timeout) {
        log.info("request params:" + timeout);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ApiResponse.ofSuccessMsg("hi, i'm from " + serverName +  ":" + port + ", orderNo:" + timeout);
    }
    
    @Override
    public void createOrder(@RequestBody OrderInfo params) {
        log.info("request Body:" + JSON.toJSON(params));
        if("100".equals(params.getUserId())) {
            throw new IllegalArgumentException();
        }
        if("tt".equals(params.getUserId())) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        params.setOrderNo("11" + System.currentTimeMillis());
        params.setUserId("Tom11");
        params.setPort(port);
        params.setCardCode(CityCodeConstant.GUANGXI);
        
        //return ApiResponse.ofSuccess(params);
    }
}
