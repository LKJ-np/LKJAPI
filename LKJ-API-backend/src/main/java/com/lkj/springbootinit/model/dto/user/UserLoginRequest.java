package com.lkj.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 账户
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
    /**
     * 邮箱
     */
    private String emailNum;
    /**
     * 验证码
     */
    private String emailCaptcha;


}
