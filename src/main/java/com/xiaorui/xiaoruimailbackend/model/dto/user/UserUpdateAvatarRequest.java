package com.xiaorui.xiaoruimailbackend.model.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 用户修改头像请求（TODO 打算使用MiniIO，参数待调整）
 * @author: xiaorui
 * @date: 2025-10-23 15:11
 **/
@Data
public class UserUpdateAvatarRequest {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户头像文件
     */
    private MultipartFile userAvatar;

}
