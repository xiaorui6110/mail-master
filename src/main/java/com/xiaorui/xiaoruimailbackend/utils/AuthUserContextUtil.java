package com.xiaorui.xiaoruimailbackend.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;

/**
 * @description: 用户认证信息上下文工具类（用于在 Spring Security 上下文中管理用户认证信息）
 * @author: xiaorui
 * @date: 2025-10-22 15:16
 **/

public class AuthUserContextUtil {

    private static final ThreadLocal<UserInfoInTokenBO> USER_INFO_IN_TOKEN_HOLDER = new TransmittableThreadLocal<>();

    public static UserInfoInTokenBO get() {

        return USER_INFO_IN_TOKEN_HOLDER.get();
    }

    public static void set(UserInfoInTokenBO userInfoInTokenBo) {
        USER_INFO_IN_TOKEN_HOLDER.set(userInfoInTokenBo);
    }

    public static void clean() {
        if (USER_INFO_IN_TOKEN_HOLDER.get() != null) {
            USER_INFO_IN_TOKEN_HOLDER.remove();
        }
    }

}
