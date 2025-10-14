package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户注册请求
 * @author: xiaorui
 * @date: 2025-10-14 21:15
 **/
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 9111406458644329827L;
    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;

}
