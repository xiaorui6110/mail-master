package com.xiaorui.xiaoruimailbackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @description: token信息，该信息用户返回给前端，前端请求携带accessToken进行用户校验
 * @author: xiaorui
 * @date: 2025-10-21 17:13
 **/
@Data
public class TokenInfoVO {
    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;

    /**
     * 过期时间
     */
    private Integer expiresTime;
}
