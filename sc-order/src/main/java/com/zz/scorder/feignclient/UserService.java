package com.zz.scorder.feignclient;

import com.zz.api.common.protocal.ApiResponse;
import com.zz.scservice.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ************************************
 * create by Intellij IDEA
 *
 * @author Francis.zz
 * @date 2022-08-25 14:39
 * ************************************
 */
@FeignClient(value = "sc-service", contextId = "user")
public interface UserService {
    /**
     * openfeign客户端传参需要加上注解直接传参方式
     *
     * @RequestHeader(“param”) 如果有中文，要进行 URL 编码，放在请求的 Head 头中
     * @RequestParam(“param”) GET 请求
     * @PathVariable(“param”) 请求路径上
     * POST 请求的 JSON不需要注解
     *
     * {@link SpringMvcContract#getDefaultAnnotatedArgumentsProcessors()} 默认支持的注解的处理器
     *
     * @param userId
     * @return
     */
    @GetMapping("/user/query")
    ApiResponse<UserInfo> query(@RequestParam String userId);

    @PostMapping("/user/create")
    ApiResponse<UserInfo> createOrder(UserInfo params);
}
