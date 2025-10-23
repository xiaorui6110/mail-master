package com.xiaorui.xiaoruimailbackend.controller;

import com.xiaorui.xiaoruimailbackend.common.BaseResponse;
import com.xiaorui.xiaoruimailbackend.common.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 健康检查
 * @author: xiaorui
 * @date: 2025-10-10 20:53
 **/
@RestController
@RequestMapping("/")
public class HealthController {
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtil.success("ok");
    }

}
