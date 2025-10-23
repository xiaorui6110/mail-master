package com.xiaorui.xiaoruimailbackend.model.bo;

import lombok.Data;

/**
 * @description: 用户详细信息
 * @author: xiaorui
 * @date: 2025-10-22 15:35
 **/
@Data
public class UserInfoBO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商家用户id
     */
    private String bizUserId;

    /**
     * 状态 1-正常 0-无效
     */
    private Boolean enabled;

    /**
     * 自提点Id
     */
    private Long stationId;

    /**
     * 店铺Id
     */
    private Long shopId;

}
