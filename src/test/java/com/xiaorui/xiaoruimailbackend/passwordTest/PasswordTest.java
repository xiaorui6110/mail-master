package com.xiaorui.xiaoruimailbackend.passwordTest;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description: 密码加密测试
 * @author: xiaorui
 * @date: 2025-10-24 15:10
 **/
@SpringBootTest
public class PasswordTest {

    @Resource
    private PasswordEncoder passwordEncoder;

    private static final String RAW_PASSWORD = "123456";

    /**
     * 测试密码加密
     */
    @Test
    public void testPasswordEncoder() {
        String encodedPassword = passwordEncoder.encode(RAW_PASSWORD);
        System.out.println("加密后的密码：" + encodedPassword);
        System.out.println("是否匹配：" + passwordEncoder.matches(RAW_PASSWORD, encodedPassword));
    }


}
