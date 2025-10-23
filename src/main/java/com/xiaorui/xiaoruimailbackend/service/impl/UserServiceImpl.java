package com.xiaorui.xiaoruimailbackend.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
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
import com.xiaorui.xiaoruimailbackend.exception.ThrowUtil;
import com.xiaorui.xiaoruimailbackend.manager.PasswordCheckManager;
import com.xiaorui.xiaoruimailbackend.manager.PasswordDecryptManager;
import com.xiaorui.xiaoruimailbackend.manager.TokenStoreManager;
import com.xiaorui.xiaoruimailbackend.model.bo.UserInfoInTokenBO;
import com.xiaorui.xiaoruimailbackend.model.dto.user.UserQueryRequest;
import com.xiaorui.xiaoruimailbackend.model.entity.User;
import com.xiaorui.xiaoruimailbackend.model.vo.TokenInfoVO;
import com.xiaorui.xiaoruimailbackend.model.vo.UserVO;
import com.xiaorui.xiaoruimailbackend.service.UserService;
import com.xiaorui.xiaoruimailbackend.mapper.UserMapper;
import com.xiaorui.xiaoruimailbackend.utils.EmailSenderUtil;
import com.xiaorui.xiaoruimailbackend.utils.PrincipalUtil;
import com.xiaorui.xiaoruimailbackend.utils.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
     * 用户登录（邮箱 + 密码登录）
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @return  用户信息vo
     */
    @Override
    public UserVO userLogin(String userEmail, String loginPassword) {
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
        // 获取客户端IP
        String clientIp = request.getRemoteAddr();
        String ipKey = String.format("email:code:ip:%s", clientIp);
        String emailKey = String.format("email:code:email:%s", userEmail);
        // 检查IP是否频繁请求验证码
        String ipCount = stringRedisTemplate.opsForValue().get(ipKey);
        if (StrUtil.isNotBlank(ipCount) && Integer.parseInt(ipCount)  > IP_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"请求验证码过于频繁，请稍后再试");
        }
        // 检查邮箱是否频繁请求验证码
        String emailCount = stringRedisTemplate.opsForValue().get(emailKey);
        if (StrUtil.isNotBlank(emailCount) && Integer.parseInt(emailCount)  > EMAIL_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"该邮箱请求验证码过于频繁，请稍后再试");
        }
        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);
        // 发送验证码
        try {
            emailSenderUtil.sendEmail(userEmail, code);
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发送验证码失败");
        }
        // 记录IP和邮箱的请求次数，设置1小时过期
        stringRedisTemplate.opsForValue().increment(ipKey, 1);
        stringRedisTemplate.opsForValue().increment(emailKey, 1);
        stringRedisTemplate.expire(ipKey,1, TimeUnit.HOURS);
        stringRedisTemplate.expire(emailKey,1, TimeUnit.HOURS);
        // 将验证码存入Redis，设置5分钟过期
        String verifyCodeKey = String.format("email:code:verify:%s:%s", type, userEmail);
        stringRedisTemplate.opsForValue().set(verifyCodeKey, code, 5, TimeUnit.MINUTES);
    }


    /**
     * 获取图形验证码（使用HutoolUtil生成
     * <a href="https://doc.hutool.cn/pages/captcha">...</a>）
     *
     * @return 图形验证码
     */
    @Override
    public Map<String, String> getPictureVerifyCode() {
        // 仅包含数字的字符集
        String characters = "0123456789";
        // 生成4位数字验证码
        RandomGenerator randomGenerator = new RandomGenerator(characters, 4);
        // 定义图片的显示大小，并创建验证码对象
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(320, 100, 4, 4);
        shearCaptcha.setGenerator(randomGenerator);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        shearCaptcha.write(outputStream);
        byte[] captchaBytes = outputStream.toByteArray();
        String base64Captcha = Base64.getEncoder().encodeToString(captchaBytes);
        String captchaCode = shearCaptcha.getCode();
        // 使用Hutool的MD5加密
        String encryptedCaptcha = DigestUtil.md5Hex(captchaCode);
        // 将加密后的验证码和Base64编码的图片存储到Redis中，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set("captcha:" + encryptedCaptcha, captchaCode, 300, TimeUnit.SECONDS);
        Map<String, String> data = new HashMap<>();
        data.put("base64Captcha", base64Captcha);
        data.put("encryptedCaptcha", encryptedCaptcha);
        return data;
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
            // 对用户输入的验证码进行MD5加密，然后与服务器存储的验证码进行比较（服务器中的存储的是MD5加密后的验证码）
            String encryptedVerifycode = DigestUtil.md5Hex(verifyCode);
            if (encryptedVerifycode.equals(serverVerifyCode)) {
                return true;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
    }


    /**
     * 获取查询条件（通过userId、nickName查询）
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 获取查询参数
        String userId = userQueryRequest.getUserId();
        String nickName = userQueryRequest.getNickName();
        ThrowUtil.throwIf(StrUtil.isBlank(userId) && StrUtil.isBlank(nickName), ErrorCode.PARAMS_ERROR, "查询条件为空");
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(nickName), "nickName", nickName);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }


    /**
     * 用户登出（退出登录）
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");
        if (StrUtil.isBlank(accessToken)) {
            return true;
        }
        // 删除该用户在该系统当前的token
        tokenStoreManager.deleteCurrentToken(accessToken);
        return true;
    }


    /**
     * 获取用户信息
     *
     * @return 用户信息vo
     */
    @Override
    public UserVO getUserInfo() {
        String userId = SecurityUtil.getUser().getUserId();
        User user = this.baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }


    /**
     * 修改用户邮箱
     *
     * @param newUserEmail 新邮箱
     * @param emailVerifyCode 邮箱验证码
     * @return 是否成功
     */
    @Override
    public boolean changeUserEmail(String newUserEmail, String emailVerifyCode) {
        // 校验用户输入信息
        if (StrUtil.hasBlank(newUserEmail, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (!newUserEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        // 校验验证码
        String verifyCodeKey = String.format("email:code:verify:changeEmail:%s", newUserEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        // 获取当前用户信息
        String userId = SecurityUtil.getUser().getUserId();
        User loginUser = this.baseMapper.selectById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 检查新邮箱是否已被使用
        synchronized (newUserEmail.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", newUserEmail);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已被使用");
            }
            // 修改邮箱
            User user = new User();
            user.setUserId(loginUser.getUserId());
            user.setUserEmail(newUserEmail);
            user.setUpdateTime(new Date());
            boolean updateResult = this.updateById(user);
            if (!updateResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改邮箱失败");
            }
            // 删除验证码
            stringRedisTemplate.delete(verifyCodeKey);
            // 返回修改结果
            return true;
        }
    }


    /**
     * 修改用户密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    @Override
    public boolean changeUserPassword(String oldPassword, String newPassword, String checkPassword) {
        // 简单的参数校验
        if (StrUtil.hasBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        if (newPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (newPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 查询用户信息
        String userId = SecurityUtil.getUser().getUserId();
        User loginUser = this.baseMapper.selectById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 将新密码解密再加密
        String decryptPassword = passwordDecryptManager.decryptPassword(newPassword);
        String encryptPassword = passwordEncoder.encode(decryptPassword);
        if (StrUtil.equals(encryptPassword, loginUser.getLoginPassword())) {
            // 新密码不能与原密码相同
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"新密码不能与原密码相同");
        }
        // 修改密码
        loginUser.setLoginPassword(encryptPassword);
        loginUser.setUpdateTime(new Date());
        boolean updateResult = this.updateById(loginUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改密码失败");
        }
        return true;
    }


    /**
     * 重置用户密码
     *
     * @param userEmail 用户邮箱
     * @param emailVerifyCode 邮箱验证码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    @Override
    public boolean resetUserPassword(String userEmail, String emailVerifyCode, String newPassword, String checkPassword) {
        // 校验信息
        if (StrUtil.hasBlank(userEmail, emailVerifyCode, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (!userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式错误");
        }
        if (newPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if (newPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过长");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        // 校验验证码
        String verifyCodeKey = String.format("email:code:verify:resetPassword:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误或已过期");
        }
        // 查询用户信息
        String userId = SecurityUtil.getUser().getUserId();
        User loginUser = this.baseMapper.selectById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 解密并加密密码
        String decryptPassword = passwordDecryptManager.decryptPassword(newPassword);
        String encryptPassword = passwordEncoder.encode(decryptPassword);
        // 重置密码
        loginUser.setLoginPassword(encryptPassword);
        loginUser.setUpdateTime(new Date());
        boolean updateResult = this.updateById(loginUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重置密码失败");
        }
        // 删除验证码
        stringRedisTemplate.delete(verifyCodeKey);
        return true;
    }


}
