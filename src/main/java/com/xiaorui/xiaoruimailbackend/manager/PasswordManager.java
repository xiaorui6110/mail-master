package com.xiaorui.xiaoruimailbackend.manager;

import cn.hutool.crypto.symmetric.AES;
import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @description: 密码加密管理
 * @author: xiaorui
 * @date: 2025-10-18 17:21
 **/
@Component
public class PasswordManager {

    private static final Logger logger = LoggerFactory.getLogger(PasswordManager.class);
    /**
     * 用于aes签名的key，16位
     */
    @Value("${auth.password.signKey:-xiaoruimail-password}")
    public String passwordSignKey;

    public String decryptPassword(String data) {
        // 在使用oracle的JDK时，JAR包必须签署特殊的证书才能使用。
        // 解决方案 1.使用openJDK或者非oracle的JDK（建议） 2.添加证书
        // hutool的aes报错可以打开下面那段代码
        // SecureUtil.disableBouncyCastle();
        AES aes = new AES(passwordSignKey.getBytes(StandardCharsets.UTF_8));
        String decryptStr;
        String decryptPassword;
        try {
            decryptStr = aes.decryptStr(data);
            decryptPassword = decryptStr.substring(13);
        } catch (Exception e) {
            logger.error("Exception:", e);
            throw new BusinessException("AES 解密错误", e);

        }
        return decryptPassword;
    }

}
