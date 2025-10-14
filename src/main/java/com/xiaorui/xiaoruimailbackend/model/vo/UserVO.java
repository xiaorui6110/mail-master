package com.xiaorui.xiaoruimailbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户信息vo
 * @author: xiaorui
 * @date: 2025-10-14 20:33
 **/
@Data
public class UserVO implements Serializable {
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

    private static final long serialVersionUID = 8721898352063782780L;

}
