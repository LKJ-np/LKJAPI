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
    isDelete     tinyint      default 0                 not null comment '是否删除,0-未删，1-已删',
    phone        varchar(512)                           null comment '电话号',
    email        varchar(512)                           null comment '邮箱',
    constraint uni_userAccount
        unique (userAccount)
)
    comment '用户信息表';


create table interface_info
(
    id               bigint auto_increment comment '主键'
        primary key,
    name             varchar(256)                       not null comment '名称',
    description      varchar(256)                       null comment '描述',
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
    comment '接口信息表';

create table user_interface_info
(
    id              bigint auto_increment comment '主键'
        primary key,
    userId          bigint                             not null comment '用户id',
    interfaceInfoId bigint                             not null comment '接口 id',
    totalNum        int      default 0                 not null comment '接口的总调用次数',
    leftNum         int      default 0                 not null comment '接口的剩余调用次数',
    status          int      default 0                 not null comment '0-正常，1-禁用',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)',
    version         int      default 0                 null comment '乐观锁版本号'
)
    comment '用户调用接口关系表';

create table interface_charging
(
    id              bigint auto_increment comment '主键'
        primary key,
    interfaceId     bigint                             not null comment '接口id',
    charging        float(255, 2)                      not null comment '计费规则（元/条）',
    availablePieces varchar(255)                       not null comment '接口剩余可调用次数（接口库存）',
    userId          bigint                             not null comment '创建人',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 1                 not null comment '是否删除(0-正常 1-删除)'
)
    charset = utf8;
    comment '接口收费表';

create table t_order
(
    id          bigint auto_increment comment '主键id'
        primary key,
    userId      bigint        null comment '用户id',
    interfaceId bigint        null comment '接口id',
    count       int           null comment '购买数量',
    totalAmount decimal       null comment '订单应付价格',
    status      int default 0 null comment '订单状态 0-未支付 1 -已支付 2-超时支付',
    isDelete    int default 1 not null comment '0-删除 1 正常',
    createTime  datetime      null on update CURRENT_TIMESTAMP comment '创建时间',
    updateTime  datetime      null on update CURRENT_TIMESTAMP comment '更新时间',
    orderSn     varchar(255)  not null comment '订单号',
    charging    float         not null comment '单价'
)
    comment '订单表';

create table alipay_info
(
    orderNumber    varchar(512) not null comment '订单id'
        primary key,
    subject        varchar(225) not null comment '交易名称',
    totalAmount    float        not null comment '交易金额',
    buyerPayAmount float        not null comment '买家付款金额',
    buyerId        text         not null comment '买家在支付宝的唯一id',
    tradeNo        text         not null comment '支付宝交易凭证号',
    tradeStatus    varchar(255) not null comment '交易状态',
    gmtPayment     datetime     not null comment '买家付款时间'
)
    comment '支付表';