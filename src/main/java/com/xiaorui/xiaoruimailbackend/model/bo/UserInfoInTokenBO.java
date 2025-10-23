package com.xiaorui.xiaoruimailbackend.model.bo;

import lombok.Data;

import java.util.Set;

/**
 * @description: 保存在token信息里面的用户信息
 * @author: xiaorui
 * @date: 2025-10-21 16:39
 **/
@Data
public class UserInfoInTokenBO {
    /**
     * 用户在自己系统的用户id
     */
    private String userId;

    /**
     * 店铺Id
     */
    private Long shopId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 系统类型
     * @see com.xiaorui.xiaoruimailbackend.enums.SysTypeEnum
     */
    private Integer sysType;

    /**
     * 是否是管理员
     */
    private Integer isAdmin;

    /**
     * 商家用户id
     */
    private String bizUserId;

    /**
     * 权限列表
     */
    private Set<String> perms;

    /**
     * 状态 1-正常 0-无效
     */
    private Boolean enabled;

    /**
     * 其他Id
     */
    private Long otherId;

}
