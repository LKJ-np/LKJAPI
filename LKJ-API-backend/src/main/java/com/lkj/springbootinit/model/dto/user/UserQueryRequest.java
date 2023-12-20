package com.lkj.springbootinit.model.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lkj.apicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author <a href="https://github.com/lkj">程序员lkj</a>
 * @from <a href="https://程序员lkj">程序员lkj</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

//    /**
//     * 开放平台id
//     */
//    private String unionId;

//    /**
//     * 公众号openId
//     */
//    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

//    /**
//     * 简介
//     */
//    private String userProfile;
    /**
     * 用户手机号
     */
    private String phoneNum;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String createTime;

    /**
     * 更新时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String updateTime;


    private static final long serialVersionUID = 1L;
}