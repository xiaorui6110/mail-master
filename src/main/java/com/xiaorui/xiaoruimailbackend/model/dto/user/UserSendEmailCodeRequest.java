package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户发送邮箱验证码请求
 * @author: xiaorui
 * @date: 2025-10-14 21:41
 **/
@Data
public class UserSendEmailCodeRequest implements Serializable {

    private static final long serialVersionUID = -5251993765730510038L;
    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 验证码用途：register-注册，resetPassword-重置密码，changeEmail-修改邮箱
     */
    private String type;

}
