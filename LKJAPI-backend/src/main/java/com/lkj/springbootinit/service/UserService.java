package com.lkj.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lkj.springbootinit.model.dto.user.UserQueryRequest;
import com.lkj.springbootinit.model.dto.user.UserUpdateRequest;
import com.lkj.springbootinit.model.vo.LoginUserVO;
import com.lkj.springbootinit.model.vo.UserDevKeyVO;
import com.lkj.springbootinit.model.vo.UserVO;
import com.lkj.apicommon.entity.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 发送邮箱验证码
     * todo 手机验证码
     * @param emailNum
     * @param captchaType
     */
    void sendCode(String emailNum, String captchaType);

    /**
     * QQ邮箱注册
     * todo 后续用手机号注册
     * @param emailNum
     * @param emailCaptcha
     * @return
     */
    long userEmailRegister(String emailNum, String emailCaptcha);

    /**
     * 用户账号密码登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 使用邮箱登录(后续会改造成使用手机号登录)
     * @param emailNum
     * @param emailCode
     * @param request
     * @param response
     * @return
     */
    LoginUserVO userLoginBySms(String emailNum, String emailCode, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 更新用户
     * @param userUpdateRequest
     * @param request
     * @return
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 上传用户头像
     * @param file
     * @param request
     * @return
     */
    boolean uploadFileAvatar(MultipartFile file, HttpServletRequest request);

    /**
     * 生成图像验证码
     * @param request
     * @param response
     */
    void getCaptcha(HttpServletRequest request, HttpServletResponse response);




    /**
     * 重新生成ak，sk
     * @param request
     * @return
     */
    UserDevKeyVO genkey(HttpServletRequest request);
}
