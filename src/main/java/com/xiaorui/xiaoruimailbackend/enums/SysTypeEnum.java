package com.xiaorui.xiaoruimailbackend.enums;

/**
 * @description: 系统类型枚举
 * @author: xiaorui
 * @date: 2025-10-18 21:02
 **/

public enum SysTypeEnum {
    /**
     * 普通用户系统
     */
    ORDINARY(0),

    /**
     * 后台
     */
    ADMIN(1);

    private final Integer value;

    public Integer value() {
        return value;
    }

    SysTypeEnum(Integer value) {
        this.value = value;
    }
}
