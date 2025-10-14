package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户修改密码请求
 * @author: xiaorui
 * @date: 2025-10-14 21:18
 **/
@Data
public class UserChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 7034520922803845631L;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

}
