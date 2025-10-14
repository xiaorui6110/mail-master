package com.xiaorui.xiaoruimailbackend.service;

import com.xiaorui.xiaoruimailbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaorui.xiaoruimailbackend.model.vo.UserVO;

/**
* @author lenovo
* @description 针对表【xr_user(用户表)】的数据库操作Service
* @createDate 2025-10-14 20:25:46
*/
public interface UserService extends IService<User> {

    /**
     *  用户注册
     *
     * @param userEmail 用户邮箱
     * @param loginPassword  登录密码
     * @param checkPassword  确认密码
     * @param emailVerifyCode  邮箱验证码
     * @return 用户id
     */
    String userRegister(String userEmail,  String loginPassword, String checkPassword, String emailVerifyCode);

    /**
     *  用户登录
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @param verifyCode    验证码（图形验证码-用户输入的）
     * @param serverVerifyCode  验证码（图形验证码-服务器存储的）
     * @return  用户信息vo
     */
    UserVO userLogin(String userEmail, String loginPassword, String verifyCode, String serverVerifyCode);


}
