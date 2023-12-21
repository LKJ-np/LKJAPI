# 数据库初始化
#重置表自增
# TRUNCATE TABLE lkjapi.user;



-- 创建数据库
create database if not exists lkjapi;

-- 切换库
use lkjapi;
-- 创建用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           null comment '密码',
    accessKey    varchar(512)                           not null comment 'accessKey',
    secretKey    varchar(512)                           not null comment 'secretKey',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    phone        varchar(512)                           null comment '电话号',
    email        varchar(512)                           null comment '邮箱',
    constraint uni_userAccount
        unique (userAccount)
)
    comment '用户';

-- 接口信息
create table if not exists lkjapi.`interface_info`
(
    id               bigint auto_increment comment '主键'
        primary key,
    name             varchar(256)                       not null comment '接口名称',
    description      varchar(256)                       null comment '接口描述',
    url              varchar(512)                       not null comment '接口地址',
    requestHeader    text                               null comment '请求头',
    responseHeader   text                               null comment '响应头',
    status           int      default 0                 not null comment '接口状态（0-关闭，1-开启）',
    method           varchar(256)                       not null comment '请求类型',
    userId           bigint                             not null comment '创建人',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)',
    requestParams    text                               null comment '请求参数',
    sdk              varchar(512)                       null comment '接口对应的sdk类路径',
    parameterExample varchar(255)                       null comment '参数示例'
)
    comment '接口信息';

-- 用户调用接口关系表
create table if not exists lkjapi.`user_interface_info`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `userId` bigint not null comment '用户id',
    `interfaceInfoId` bigint not null comment '接口 id',
    `totalNum` int default 0 not null comment '接口的总调用次数',
    `leftNum` int default 0 not null comment '接口的剩余调用次数',
    `status` int default 0 not null comment '0-正常，1-禁用',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    `version` int(0) NULL DEFAULT 0 COMMENT '乐观锁版本号'
) comment '用户调用接口关系';

-- 接口价格表
create table if not exists lkjapi.`interface_charging`(
                                       `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `interfaceId` bigint(0) NOT NULL COMMENT '接口id',
                                       `charging` float(255, 2) NOT NULL COMMENT '计费规则（元/条）',
                                       `availablePieces` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接口剩余可调用次数',
                                       `userId` bigint(0) NOT NULL COMMENT '创建人',
                                       `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                       `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                       `isDelete` tinyint(0) NOT NULL DEFAULT 1 COMMENT '是否删除(0-删除 1-正常)',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;