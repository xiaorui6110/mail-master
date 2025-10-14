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

    @Override
    public String userRegister(String userEmail, String loginPassword, String checkPassword, String emailVerifyCode) {
        return null;
    }

    @Override
    public UserVO userLogin(String userEmail, String loginPassword, String verifyCode, String serverVerifyCode) {
        return null;
    }
}




