package com.xiaorui.xiaoruimailbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 用户表
 * @author lenovo
 * @TableName xr_user
 */
@TableName(value ="xr_user")
@Data
public class User implements Serializable {
    /**
     * 用户id（修改 id 生成的策略为 ASSIGN_ID 雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别 m-男 f-女
     */
    private String userSex;

    /**
     * 用户生日 yyyy-mm-dd
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private String userBirthday;

    /**
     * 用户备注
     */
    private String userProfile;

    /**
     * 用户状态 1-正常 2-禁用
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 注册ip
     */
    private String userRegip;

    /**
     * 最后登录ip
     */
    private String userLastip;

    /**
     * 最后登录时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date userLasttime;

    /**
     * 用户积分
     */
    private Integer userScore;

    /**
     * 是否删除 0-未删除 1-已删除（逻辑删除）
     */
    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = -4188525966388299528L;
}