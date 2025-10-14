package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户修改邮箱请求
 * @author: xiaorui
 * @date: 2025-10-14 21:17
 **/
@Data
public class UserChangeEmailRequest implements Serializable {

    private static final long serialVersionUID = 3722664261410053383L;
    /**
     * 新邮箱
     */
    private String newEmail;

    /**
     * 验证码（邮箱验证码）
     */
    private String emailVerifyCode;

}
