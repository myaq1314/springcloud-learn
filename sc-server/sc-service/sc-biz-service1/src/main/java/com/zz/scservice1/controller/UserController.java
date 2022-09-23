package com.zz.scservice1.controller;

import com.alibaba.fastjson.JSON;
import com.zz.api.common.protocal.ApiResponse;
import com.zz.api.common.protocal.CityCodeConstant;
import com.zz.scservice.entity.OrderInfo;
import com.zz.scservice.entity.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

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
    @GetMapping("/query")
    public ApiResponse<UserInfo> query(String userId) {
        log.info("query user" + userId);
        UserInfo res = UserInfo.of("Tom", userId, new Date());

        return ApiResponse.ofSuccess(res);
    }

    @PostMapping("/create")
    public ApiResponse<UserInfo> createOrder(@RequestBody UserInfo params) {
        log.info("request Body:" + JSON.toJSONString(params));
        params.setUserName("Jerry");
        params.setLastedTime(new Date());

        return ApiResponse.ofSuccess(params);
    }
}
