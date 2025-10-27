package com.xiaorui.xiaoruimailbackend.config;

import cn.hutool.core.util.ArrayUtil;
import com.xiaorui.xiaoruimailbackend.adapter.AuthConfigAdapter;
import com.xiaorui.xiaoruimailbackend.adapter.DefaultAuthConfigAdapter;
import com.xiaorui.xiaoruimailbackend.filter.AuthFilter;
import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @description: 授权配置
 * @author: xiaorui
 * @date: 2025-10-27 16:28
 **/
@Configuration
@EnableMethodSecurity
public class AuthConfig {

    @Resource
    private AuthFilter authFilter;

    @Bean
    @ConditionalOnMissingBean
    public AuthConfigAdapter authConfigAdapter() {
        return new DefaultAuthConfigAdapter();
    }

    @Bean
    @Lazy
    public FilterRegistrationBean<AuthFilter> filterRegistration(AuthConfigAdapter authConfigAdapter) {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        // 添加过滤器
        registration.setFilter(authFilter);
        // 设置过滤路径，/*所有路径
        registration.addUrlPatterns(ArrayUtil.toArray(authConfigAdapter.pathPatterns(), String.class));
        registration.setName("authFilter");
        // 设置优先级
        registration.setOrder(0);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }

}