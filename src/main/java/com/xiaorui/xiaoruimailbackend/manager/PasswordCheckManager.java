package com.xiaorui.xiaoruimailbackend.manager;

import cn.hutool.core.util.StrUtil;
import com.xiaorui.xiaoruimailbackend.enums.SysTypeEnum;
import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import com.xiaorui.xiaoruimailbackend.utils.IpHelper;
import com.xiaorui.xiaoruimailbackend.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @description: 密码验证管理
 * @author: xiaorui
 * @date: 2025-10-18 17:22
 **/
@Component
public class PasswordCheckManager {

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 半小时内最多错误10次
     */
    private static final int TIMES_CHECK_INPUT_PASSWORD_NUM = 10;

    /**
     * 检查用户输入错误的验证码次数
     */
    private static final String CHECK_VALID_CODE_NUM_PREFIX = "checkUserInputErrorPassword_";

    /**
     * 检查密码
     *
     * @param sysTypeEnum 系统类型
     * @param userNameOrMobile 用户名或手机号
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     */
    public void checkPassword(SysTypeEnum sysTypeEnum, String userNameOrMobile, String rawPassword, String encodedPassword) {

        String checkPrefix = sysTypeEnum.value() + CHECK_VALID_CODE_NUM_PREFIX + IpHelper.getIpAddr();

        int count = 0;
        if(RedisUtil.hasKey(checkPrefix + userNameOrMobile)){
            count = RedisUtil.get(checkPrefix + userNameOrMobile);
        }
        if(count > TIMES_CHECK_INPUT_PASSWORD_NUM){
            throw new BusinessException("密码输入错误十次，已限制登录30分钟");
        }
        // 半小时后失效
        RedisUtil.set(checkPrefix + userNameOrMobile,count, 1800);
        // 密码不正确
        if (StrUtil.isBlank(encodedPassword) || !passwordEncoder.matches(rawPassword, encodedPassword)){
            count++;
            // 半小时后失效
            RedisUtil.set(checkPrefix + userNameOrMobile, count, 1800);
            throw new BusinessException("账号或密码不正确");
        }
    }
}
