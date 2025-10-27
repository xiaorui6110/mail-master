create database IF NOT EXISTS `xiaorui_mail` default character set utf8mb4 collate utf8mb4_general_ci;

use xiaorui_mail;

-- 用户表

drop table IF EXISTS `xr_user`;

create table `xr_user`
(
    `user_id`        varchar(36) not null default '' comment '用户id',
    `nick_name`      varchar(50)          default null comment '用户昵称',
    `real_name`      varchar(50)          default null comment '真实姓名',
    `user_email`     varchar(100)         default null comment '用户邮箱',
    `login_password` varchar(255)         default null comment '登录密码',
    `pay_password`   varchar(50)          default null comment '支付密码',
    `user_phone`     varchar(50)          default null comment '用户手机号',
    `user_avatar`    varchar(255)         default null comment '用户头像',
    `user_sex`       char(1)              default null comment '用户性别 m-男 f-女',
    `user_birthday`  char(10)             default null comment '用户生日 yyyy-mm-dd',
    `user_profile`   varchar(500)         default null comment '用户备注',
    `user_status`    tinyint(4)           default '1' comment '用户状态 1-正常 2-禁用',
    `create_time`    datetime             default null comment '创建时间',
    `update_time`    datetime             default null comment '更新时间',
    `user_regip`     varchar(50)          default null comment '注册ip',
    `user_lastip`    varchar(50)          default null comment '最后登录ip',
    `user_lasttime`  datetime             default null comment '最后登录时间',
    `user_score`     int(11)              default null comment '用户积分',
    `is_deleted`     tinyint(4)           default '0' comment '是否删除 0-未删除 1-已删除',
    primary key (`user_id`),
    unique key `ud_user_email` (`user_email`),
    unique key `ud_unique_user_phone` (`user_phone`)
) engine = InnoDB  default charset = utf8mb4 comment '用户表';

create index `idx_user_id` on `xr_user` (`nick_name`);
create index `idx_user_phone` on `xr_user` (`user_phone`);
create index `idx_user_email` on `xr_user` (`user_email`);
















