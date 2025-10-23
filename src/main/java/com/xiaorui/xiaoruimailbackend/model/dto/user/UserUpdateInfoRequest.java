package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户修改信息请求
 * @author: xiaorui
 * @date: 2025-10-14 21:16
 **/
@Data
public class UserUpdateInfoRequest implements Serializable {

    private static final long serialVersionUID = 3551892461806128940L;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别 m-男 f-女
     */
    private String userSex;

    /**
     * 用户生日 yyyy-mm-dd
     */
    private String userBirthday;

    /**
     * 用户备注
     */
    private String userProfile;

}
