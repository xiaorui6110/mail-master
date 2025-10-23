package com.xiaorui.xiaoruimailbackend.utils;

import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import com.xiaorui.xiaoruimailbackend.exception.ErrorCode;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoBO;
import lombok.experimental.UtilityClass;

/**
 * @description: Spring Security 工具类（从上下文中获取用户信息）
 * @author: xiaorui
 * @date: 2025-10-22 15:30
 **/
@UtilityClass
public class SecurityUtil {

    private static final String USER_REQUEST = "/user/";

    /**
     * 获取用户
     */
    public UserInfoBO getUser() {
        if (!HttpContextUtil.getHttpServletRequest().getRequestURI().startsWith(USER_REQUEST)) {
            // 用户相关的请求，应该以/user开头
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户相关的请求，应该以/user开头");
        }
        UserInfoInTokenBO userInfoInTokenBO = AuthUserContextUtil.get();
        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserId(userInfoInTokenBO.getUserId());
        userInfoBO.setBizUserId(userInfoInTokenBO.getBizUserId());
        userInfoBO.setEnabled(userInfoInTokenBO.getEnabled());
        userInfoBO.setShopId(userInfoInTokenBO.getShopId());
        userInfoBO.setStationId(userInfoInTokenBO.getOtherId());
        return userInfoBO;
    }
}
