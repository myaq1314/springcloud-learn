package com.zz.scservice.feignapi;

import com.zz.api.common.protocal.ApiResponse;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.fallback.OrderClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ************************************
 * create by Intellij IDEA
 * FeignClient name参数是请求到具体后端服务的服务名
 * @author Francis.zz
 * @date 2020-04-23 14:20
 * ************************************
 */
@FeignClient(value = "sc-service", /*fallback = OrderClientFallback.class, */fallbackFactory = OrderClientFactory.class)
public interface OrderClient {
    /**
     * FeignClient的configuration默认为 FeignClientsConfiguration
     * 当请求参数没有 @RequestBody、@RequestParam、@Param等注解时需要自定义encoder实现，不实现的话默认SpringEncoder
     * @see {@link feign.codec.Encoder}
     *
     * FeignClient注解的value指定服务提供者的服务名
     *
     * @EnableFeignClients 注解导入了 {@link org.springframework.cloud.openfeign.FeignClientsRegistrar} 注册器，
     * 会扫描指定包下的@FeignClient注解的类或接口，获取其信息注册为bean。
     *
     * feign客户端请求参数需要使用注解来指定传参格式
     *
     * 配置的fallback或者fallbackFactory是在调用下游服务出错时将fallback指定类的返回作为该接口的返回。
     */
    @GetMapping("/getOrder")
    ApiResponse<String> getOrderInfo(@RequestParam int timeout);
    
    @PostMapping("/createOrder")
    void createOrder(@RequestBody OrderInfo order);
}
