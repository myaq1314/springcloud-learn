package com.zz.scorder.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.scorder.feignclient.UserService;
import com.zz.scservice.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2020-05-19 17:01
 * ************************************
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 演示使用openfeign客户端调用下游服务，不需要再手动使用httpclient等工具调用，openfeign集成负载均衡（引入loadbanlancer依赖）
     *
     * @param userId
     * @return
     */
    @GetMapping("/query")
    public ApiResponse<UserInfo> query(String userId) {
        log.info("query user" + userId);

        return userService.query(userId);
    }

    @PostMapping("/create")
    public ApiResponse<UserInfo> createOrder(@RequestBody UserInfo params) {
        log.info("request Body:" + JSON.toJSONString(params));

        return userService.createOrder(params);
    }
}
