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

    private String code;

    private T data;

    private String msg;

    public BaseResponse(String code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public BaseResponse(String code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMsg());
    }

}
