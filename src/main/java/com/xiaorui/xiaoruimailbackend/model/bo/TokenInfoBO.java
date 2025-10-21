package com.xiaorui.xiaoruimailbackend.model.bo;

import lombok.Data;

/**
 * @description: token信息，该信息存在redis中
 * @author: xiaorui
 * @date: 2025-10-21 16:39
 **/
@Data
public class TokenInfoBO {
    /**
     * 保存在token信息里面的用户信息
     */
    private UserInfoInTokenBO userInfoInToken;

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
