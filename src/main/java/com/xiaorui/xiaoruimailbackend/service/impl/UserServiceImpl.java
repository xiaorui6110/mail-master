package com.xiaorui.xiaoruimailbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaorui.xiaoruimailbackend.enums.SysTypeEnum;
import com.xiaorui.xiaoruimailbackend.enums.UserStatusEnum;
import com.xiaorui.xiaoruimailbackend.exception.BusinessException;
import com.xiaorui.xiaoruimailbackend.exception.ErrorCode;
import com.xiaorui.xiaoruimailbackend.manager.PasswordCheckManager;
import com.xiaorui.xiaoruimailbackend.manager.PasswordDecryptManager;
import com.xiaorui.xiaoruimailbackend.manager.TokenStoreManager;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;
import com.xiaorui.xiaoruimailbackend.model.dto.user.UserQueryRequest;
import com.xiaorui.xiaoruimailbackend.model.entity.User;
import com.xiaorui.xiaoruimailbackend.model.vo.TokenInfoVO;
import com.xiaorui.xiaoruimailbackend.model.vo.UserVO;
import com.xiaorui.xiaoruimailbackend.response.ServerResponseEntity;
import com.xiaorui.xiaoruimailbackend.service.UserService;
import com.xiaorui.xiaoruimailbackend.mapper.UserMapper;
import com.xiaorui.xiaoruimailbackend.utils.EmailSenderUtil;
import com.xiaorui.xiaoruimailbackend.utils.PrincipalUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
* @author lenovo
* @description 针对表【xr_user(用户表)】的数据库操作Service实现
* @createDate 2025-10-14 20:25:46
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private static final int USERPASSWORD_MIN_LENGTH = 6;

    private static final int USERPASSWORD_MAX_LENGTH = 36;

    private static final int IP_REQUEST_LIMIT = 5;

    private static final int EMAIL_REQUEST_LIMIT = 3;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordDecryptManager passwordDecryptManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private EmailSenderUtil emailSenderUtil;

    @Resource
    private PasswordCheckManager passwordCheckManager;

    @Resource
    private TokenStoreManager tokenStoreManager;

    /**
     * 用户注册（使用邮箱进行注册）
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
        if (StrUtil.hasBlank(userEmail, loginPassword, checkPassword, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        if (loginPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (loginPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        if (!loginPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 校验验证码
        String verifyCodeKey = String.format("email:code:verify:register:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        // 检查邮箱是否已被注册
        synchronized (userEmail.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", userEmail);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邮箱已被注册");
            }
            // 注册用户信息
            User user = new User();
            user.setUserId(IdUtil.simpleUUID());
            user.setNickName(userEmail.substring(0, userEmail.indexOf("@")));
            user.setUserEmail(userEmail);
            // 先解密再加密存储到数据库（用户输入密码传输到后端的过程中，密码是使用AES加密的，之后先解密再使用BCrypt加密存储）
            // Spring Security 5.x 及以上版本: passwordEncoder.encode() 默认使用 BCryptPasswordEncoder
            user.setLoginPassword(passwordEncoder.encode(passwordDecryptManager.decryptPassword(loginPassword)));
            user.setUserStatus(UserStatusEnum.NORMAL.getValue());
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            boolean result = this.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户注册失败");
            }
            // 删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
            // 返回用户ID
            return user.getUserId();
        }
    }


    /**
     * 用户登录（邮箱、密码登录）
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @param request  HTTP请求
     * @return  用户信息vo
     */
    @Override
    public UserVO userLogin(String userEmail, String loginPassword, HttpServletRequest request) {
        // 校验数据
        if (StrUtil.hasBlank(userEmail, loginPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        if (loginPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (loginPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        // 查询用户信息是否存在
        User user = null;
        if (PrincipalUtil.isEmail(userEmail)) {
            user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserEmail, userEmail));
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        // 输入密码先解密再加密，用于登录查询
        String decryptPassword = passwordDecryptManager.decryptPassword(loginPassword);
        // 半小时内密码输入错误十次，已限制登录30分钟
        passwordCheckManager.checkPassword(SysTypeEnum.ORDINARY, user.getNickName(), decryptPassword, user.getLoginPassword());
        // 将用户信息存储到token中
        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(user.getUserId());
        userInfoInToken.setSysType(SysTypeEnum.ORDINARY.getValue());
        userInfoInToken.setEnabled(user.getUserStatus() == 1);
        // 存储token（暂时考虑不需要返回tokenVO）
        TokenInfoVO tokenInfoVO = tokenStoreManager.storeAndGetVo(userInfoInToken);
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }


    /**
     * 发送邮箱验证码（用户点击发送验证码）
     *
     * @param userEmail 邮箱
     * @param type 验证类型
     * @param request HTTP请求
     */
    @Override
    public void sendEmailCode(String userEmail, String type, HttpServletRequest request) {
        if (StrUtil.hasBlank(userEmail, type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        // TODO 检测高频操作

        // 检查邮箱格式
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        // 获取客户端 IP
        String clientIp = request.getRemoteAddr();
        String ipKey = String.format("email:code:ip:%s", clientIp);
        String emailKey = String.format("email:code:email:%s", userEmail);
        // 检查 IP 是否频繁请求验证码
        String ipCount = stringRedisTemplate.opsForValue().get(ipKey);
        if (ipCount != null && Integer.parseInt(ipCount)  > IP_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"请求验证码过于频繁，请稍后再试");
        }
        // 检查邮箱是否频繁请求验证码
        String emailCount = stringRedisTemplate.opsForValue().get(emailKey);
        if (emailCount != null && Integer.parseInt(emailCount)  > EMAIL_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"该邮箱请求验证码过于频繁，请稍后再试");
        }
        // 生成 6 位验证码
        String code = RandomUtil.randomNumbers(6);
        // 发送验证码
        try {
            emailSenderUtil.sendEmail(userEmail, code);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送验证码失败");
        }
        // 记录 IP 和邮箱的请求次数，设置 1 小时过期
        stringRedisTemplate.opsForValue().increment(ipKey, 1);
        stringRedisTemplate.opsForValue().increment(emailKey, 1);
        stringRedisTemplate.expire(ipKey,1, TimeUnit.HOURS);
        stringRedisTemplate.expire(emailKey,1, TimeUnit.HOURS);
        // 将验证码存入 Redis，设置 5 分钟过期
        String verifyCodeKey = String.format("email:code:verify:%s:%s", type, userEmail);
        stringRedisTemplate.opsForValue().set(verifyCodeKey, code, 5, TimeUnit.MINUTES);
    }


    /**
     * 校验图形验证码（从登录逻辑中抽离出来）
     *
     * @param verifyCode 用户输入的验证码
     * @param serverVerifyCode 服务器存储的验证码
     * @return 是否正确
     */
    @Override
    public boolean checkPictureVerifyCode(String verifyCode, String serverVerifyCode) {
        if (verifyCode != null && serverVerifyCode != null) {
            // 对用户输入的验证码进行 MD5 加密，然后与服务器存储的验证码进行比较
            String encryptedVerifycode = DigestUtil.md5Hex(verifyCode);
            if (encryptedVerifycode.equals(serverVerifyCode)) {
                return true;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
    }


    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return null;
    }
}




