package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户解封/封禁请求
 * @author: xiaorui
 * @date: 2025-10-14 21:19
 **/
@Data
public class UserUnbanRequest implements Serializable {

    private static final long serialVersionUID = -93204234733322139L;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 操作类型：true-解禁，false-封禁
     */
    private Boolean isUnban;
}
