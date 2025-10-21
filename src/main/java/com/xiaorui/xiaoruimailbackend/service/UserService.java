package com.xiaorui.xiaoruimailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaorui.xiaoruimailbackend.model.dto.user.UserQueryRequest;
import com.xiaorui.xiaoruimailbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaorui.xiaoruimailbackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author lenovo
* @description 针对表【xr_user(用户表)】的数据库操作Service
* @createDate 2025-10-14 20:25:46
*/
public interface UserService extends IService<User> {



    /**
     * 用户注册（使用邮箱进行注册）
     *
     * @param userEmail 用户邮箱
     * @param loginPassword  登录密码
     * @param checkPassword  确认密码
     * @param emailVerifyCode  邮箱验证码
     * @return 用户id
     */
    String userRegister(String userEmail,  String loginPassword, String checkPassword, String emailVerifyCode);

    /**
     * 用户登录（邮箱、密码登录）
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @param request  HTTP请求
     * @return 用户信息vo
     */
    UserVO userLogin(String userEmail, String loginPassword, HttpServletRequest request);

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param type 验证类型
     * @param request HTTP请求
     */
    void sendEmailCode(String email, String type, HttpServletRequest request);

    /**
     * 校验图形验证码（从登录逻辑中抽离出来）
     *
     * @param verifyCode 用户输入的验证码
     * @param serverVerifyCode 服务器存储的验证码
     * @return 是否正确
     */
    boolean checkPictureVerifyCode(String verifyCode, String serverVerifyCode);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);



}
