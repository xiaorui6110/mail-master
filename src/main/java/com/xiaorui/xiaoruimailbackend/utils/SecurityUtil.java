package com.xiaorui.xiaoruimailbackend.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.xiaorui.xiaoruimailbackend.constants.OauthCacheConstant;
import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import com.xiaorui.xiaoruimailbackend.exception.ErrorCode;
import com.xiaorui.xiaoruimailbackend.manager.TokenStoreManager;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoBO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @description: Spring Security 工具类（从上下文中获取用户信息）
 * @author: xiaorui
 * @date: 2025-10-22 15:30
 **/
@Slf4j
@UtilityClass
public class SecurityUtil {

    private static StringRedisTemplate stringRedisTemplate;

    private static TokenStoreManager tokenStoreManager;

    static {
        // 通过 ApplicationContext 获取 Bean
        ApplicationContext context = SpringContextUtil.getApplicationContext();
        stringRedisTemplate = context.getBean(StringRedisTemplate.class);
        tokenStoreManager = context.getBean(TokenStoreManager.class);
    }
    /**
     * 获取用户
     */
    public static UserInfoBO getUser() {
        String token = StpUtil.getTokenValue();
        String keyName = OauthCacheConstant.USER_INFO + token;
        // 从redis中获取token
        String userInfoInTokenBOInRedis = stringRedisTemplate.opsForValue().get(keyName);
        log.info("缓存的内容为：{}",userInfoInTokenBOInRedis);
        // 缓存的内容为："{\"enabled\":true,\"sysType\":0,\"userId\":\"1982718691089670145\"}"
        try {
            if (StrUtil.isNotEmpty(userInfoInTokenBOInRedis)) {
                // 去除外层多余的双引号和转义字符
                userInfoInTokenBOInRedis = userInfoInTokenBOInRedis.replace("\\\"", "\"");
                if (userInfoInTokenBOInRedis.startsWith("\"") && userInfoInTokenBOInRedis.endsWith("\"")) {
                    userInfoInTokenBOInRedis = userInfoInTokenBOInRedis.substring(1, userInfoInTokenBOInRedis.length() - 1);
                }
            }
        } catch (JSONException e) {
            log.error("JSON解析失败，原始内容：{}", userInfoInTokenBOInRedis, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息解析失败");
        }
        // 将缓存中的内容转换为UserInfoInTokenBO对象
        UserInfoInTokenBO userInfoInTokenBO = JSON.parseObject(userInfoInTokenBOInRedis, UserInfoInTokenBO.class);
        log.info("解析结果userInfoInTokenBO: {}",userInfoInTokenBO);
        // 添加空值检查
        if (userInfoInTokenBO == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录或登录已过期");
        }
        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserId(userInfoInTokenBO.getUserId());
        userInfoBO.setBizUserId(userInfoInTokenBO.getBizUserId());
        userInfoBO.setEnabled(userInfoInTokenBO.getEnabled());
        userInfoBO.setShopId(userInfoInTokenBO.getShopId());
        userInfoBO.setStationId(userInfoInTokenBO.getOtherId());
        return userInfoBO;
    }
}
