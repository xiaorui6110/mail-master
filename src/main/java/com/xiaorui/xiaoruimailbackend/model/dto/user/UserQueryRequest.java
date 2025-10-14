package com.xiaorui.xiaoruimailbackend.model.dto.user;

import com.xiaorui.xiaoruimailbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description: 用户查询请求
 * @author: xiaorui
 * @date: 2025-10-14 21:16
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable{

    private static final long serialVersionUID = -1571083789287826879L;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

}
