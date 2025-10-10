package com.xiaorui.xiaoruimailbackend.exception;

import lombok.Getter;

/**
 * @description: 自定义业务异常
 * @author: xiaorui
 * @date: 2025-10-10 20:35
 **/
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
