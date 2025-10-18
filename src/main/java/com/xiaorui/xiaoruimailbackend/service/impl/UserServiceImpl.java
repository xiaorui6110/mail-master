package com.xiaorui.xiaoruimailbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaorui.xiaoruimailbackend.model.entity.User;
import com.xiaorui.xiaoruimailbackend.model.vo.UserVO;
import com.xiaorui.xiaoruimailbackend.service.UserService;
import com.xiaorui.xiaoruimailbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【xr_user(用户表)】的数据库操作Service实现
* @createDate 2025-10-14 20:25:46
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{




    /**
     *  用户注册
     *
     * @param userEmail 用户邮箱
     * @param loginPassword  登录密码
     * @param checkPassword  确认密码
     * @param emailVerifyCode  邮箱验证码
     * @return 用户id
     */
    @Override
    public String userRegister(String userEmail, String loginPassword, String checkPassword, String emailVerifyCode) {
        // 校验数据



        // 验证码确认



        // 判断是否已注册



        // 保存用户信息


        // TODO 思考：用户注册后是否直接自动登录平台，还是先注册后去登录






        return null;
    }


    /**
     *  用户登录
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @param verifyCode    验证码（图形验证码-用户输入的）
     * @param serverVerifyCode  验证码（图形验证码-服务器存储的）
     * @return  用户信息vo
     */
    @Override
    public UserVO userLogin(String userEmail, String loginPassword, String verifyCode, String serverVerifyCode) {


        return null;
    }
}




