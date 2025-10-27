package com.xiaorui.xiaoruimailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @description: 密码加密配置
 * @author: xiaorui
 * @date: 2025-10-21 16:41
 **/
@Slf4j
@Configuration
@EnableWebSecurity
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder initialized");
        // 使用 BCrypt 加密
        return new BCryptPasswordEncoder();
    }

}
