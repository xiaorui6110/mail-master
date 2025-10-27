package com.xiaorui.xiaoruimailbackend.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.xiaoruimailbackend.adapter.AuthConfigAdapter;
import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import com.xiaorui.xiaoruimailbackend.handler.HttpHandler;
import com.xiaorui.xiaoruimailbackend.manager.TokenStoreManager;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;
import com.xiaorui.xiaoruimailbackend.response.ResponseEnum;
import com.xiaorui.xiaoruimailbackend.response.ServerResponseEntity;
import com.xiaorui.xiaoruimailbackend.utils.AuthUserContextUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

/**
 * @description: 授权过滤，只要实现AuthConfigAdapter接口，添加对应路径即可
 * @author: xiaorui
 * @date: 2025-10-27 16:16
 **/
@Component
public class AuthFilter implements Filter {

    @Resource
    private AuthConfigAdapter authConfigAdapter;

    @Resource
    private HttpHandler httpHandler;

    @Resource
    private TokenStoreManager tokenStoreManager;

    @Value("${sa-token.token-name:xiaorui}")
    private String tokenName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 将请求和响应对象转换为HTTP对象
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 获取请求URI
        String requestURL = req.getRequestURI();
        // 获取需要排除授权的路径列表
        List<String> excludePathPatterns = authConfigAdapter.excludePathPatterns();
        // 创建路径匹配器
        AntPathMatcher pathMatcher = new AntPathMatcher();
        // 如果匹配不需要授权的路径，就不需要校验是否需要授权
        if (CollectionUtil.isNotEmpty(excludePathPatterns)) {
            for (String excludePathPattern : excludePathPatterns) {
                if (pathMatcher.match(excludePathPattern, requestURL)) {
                    chain.doFilter(req, resp);
                    return;
                }
            }
        }
        String accessToken = req.getHeader(tokenName);
        // 也许需要登录，不登陆也能用的uri
        boolean mayAuth = pathMatcher.match(AuthConfigAdapter.MAYBE_AUTH_URI, requestURL);
        UserInfoInTokenBO userInfoInToken = null;
        try {
            // 如果有token，就要获取token
            if (StrUtil.isNotBlank(accessToken)) {
                // 校验登录，并从缓存中取出用户信息
                try {
                    StpUtil.checkLogin();
                } catch (Exception e) {
                    httpHandler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
                    return;
                }
                userInfoInToken = tokenStoreManager.getUserInfoByAccessToken(accessToken, true);
            }
            else if (!mayAuth) {
                // 返回前端401
                httpHandler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
                return;
            }
            // 保存上下文
            AuthUserContextUtil.set(userInfoInToken);
            chain.doFilter(req, resp);
        } catch (Exception e) {
            // 手动捕获下非controller异常
            if (e instanceof BusinessException) {
                httpHandler.printServerResponseToWeb((BusinessException) e);
            } else {
                throw e;
            }
        } finally {
            AuthUserContextUtil.clean();
        }
    }

}
