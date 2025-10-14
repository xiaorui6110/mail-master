package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户重置密码请求
 * @author: xiaorui
 * @date: 2025-10-14 21:18
 **/
@Data
public class UserResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = -7027559486058284396L;
    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;
}
