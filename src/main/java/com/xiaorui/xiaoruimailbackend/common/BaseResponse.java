package com.xiaorui.xiaoruimailbackend.common;

import com.xiaorui.xiaoruimailbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 通用响应类
 * @author: xiaorui
 * @date: 2025-10-10 20:40
 **/
@Data
public class BaseResponse <T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
