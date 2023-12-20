package com.lkj.springbootinit.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lkj.springbootinit.constant.CommonConstant;
import com.lkj.springbootinit.mapper.UserMapper;
import com.lkj.springbootinit.model.dto.user.UserQueryRequest;
import com.lkj.springbootinit.model.dto.user.UserUpdateRequest;
import com.lkj.springbootinit.model.enums.UserRoleEnum;
import com.lkj.springbootinit.model.vo.LoginUserVO;
import com.lkj.springbootinit.model.vo.UserDevKeyVO;
import com.lkj.springbootinit.model.vo.UserVO;
import com.lkj.springbootinit.service.UserService;
import com.lkj.springbootinit.utils.FileUploadUtil;
import com.lkj.springbootinit.utils.LeakyBucket;
import com.lkj.springbootinit.utils.SqlUtils;
import com.lkj.apicommon.common.ErrorCode;
import com.lkj.apicommon.common.JwtUtils;
import com.lkj.apicommon.exception.BusinessException;
import com.lkj.apicommon.exception.ThrowUtils;
import com.lkj.apicommon.entity.SmsMessage;
import com.lkj.apicommon.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lkj.springbootinit.constant.UserConstant.USER_LOGIN_STATE;
import static com.lkj.springbootinit.utils.LeakyBucket.loginLeakyBucket;
import static com.lkj.springbootinit.utils.LeakyBucket.registerLeakyBucket;
import static com.lkj.apicommon.constant.RabbitmqConstant.EXCHANGE_SMS_INFORM;
import static com.lkj.apicommon.constant.RabbitmqConstant.ROUTINGKEY_SMS;
import static com.lkj.apicommon.constant.RedisConstant.LOGINCODEPRE;

/**
 *
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "lkj";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Gson gson;

    /**
     * 图片验证码 redis 前缀
     */
    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    //登录和注册的标识，方便切换不同的令牌桶来限制验证码发送
    private static final String LOGIN_SIGN = "login";

    private static final String REGISTER_SIGN="register";

    public static final String USER_LOGIN_EMAIL_CODE ="user:login:email:code:";
    public static final String USER_REGISTER_EMAIL_CODE ="user:register:email:code:";

    /**
     * 账号密码注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 账户不能含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账号不合法");
        }
        //使用synchronized进行加锁，防止多个线程之间访问资源的同步性，可以保证被它修饰的方法或者代码块在任意时刻只能有一个线程执行。
        //只有一个用户可以注册
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            //分配accessKey，secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(4));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 发送邮箱验证码
     * @param email
     * @param captchaType
     */
    @Override
    public void sendCode(String email, String captchaType) {
        //判断输入的验证码
        if (StringUtils.isBlank(captchaType)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码类型为空!!!");
        }

        //令牌桶算法实现短信接口的限流，因为手机号码重复发送短信，要进行流量控制
        //解决同一个手机号的并发问题，锁的粒度非常小，不影响性能。只是为了防止用户第一次发送短信时的恶意调用
        //使用电子邮件地址作为锁的代码块来处理并发
        synchronized (email.intern()) {
            //检查在redis里面是否存储已经存在的登录邮箱key
            Boolean exist = stringRedisTemplate.hasKey(USER_LOGIN_EMAIL_CODE +email);
            //如果键存在，则表示最近已发送过验证码。然后就可以计算和上一次发送验证码的时间是否在60s内，使用自定义的令牌桶算法
            if (exist != null && exist) {
                //1.令牌桶算法对手机短信接口进行限流 具体限流规则为同一个手机号，60s只能发送一次
                long lastTime= 0L;
                LeakyBucket leakyBucket = null;
                //根据验证码的类型选择响应的键和令牌桶
                if (captchaType.equals(REGISTER_SIGN)){
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_REGISTER_EMAIL_CODE + email);
                    if (strLastTime != null){
                        lastTime = Long.parseLong(strLastTime);
                    }
                    //注册令牌桶
                    leakyBucket = registerLeakyBucket;
                }else{
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_LOGIN_EMAIL_CODE + email);
                    if (strLastTime!=null){
                        lastTime = Long.parseLong(strLastTime);
                    }
                    leakyBucket = loginLeakyBucket;
                }
                //如果邮箱在60s内一直发送，则发送太频繁
                if (!leakyBucket.control(lastTime)) {
                    log.info("邮箱发送太频繁了");
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱发送太频繁了");
                }
            }
            //2.符合限流规则则生成随机验证码
            String code = RandomUtil.randomNumbers(4);
            // 创建包含电子邮件地址和验证码的SmsMessage对象
            SmsMessage smsMessage = new SmsMessage(email, code);

            //消息队列异步发送短信，提高短信的吞吐量
            //异步发送SmsMessage对象到指定的的交换机EXCHANGE_SMS_INFORM 和路由键 ROUTINGKEY_SMS
            //todo 看rabbitmq
            rabbitTemplate.convertAndSend(EXCHANGE_SMS_INFORM,ROUTINGKEY_SMS,smsMessage);

            log.info("邮箱对象："+smsMessage.toString());
            //更新与电子邮件地址对应的Redis键，使用当前时间戳指示最后一次发送验证码的时间。该键是特定于验证码类型（注册或登录）的。
            if (captchaType.equals(REGISTER_SIGN)){
                stringRedisTemplate.opsForValue().set(USER_REGISTER_EMAIL_CODE +email,""+System.currentTimeMillis()/1000);
            }else {
                stringRedisTemplate.opsForValue().set(USER_LOGIN_EMAIL_CODE +email,""+System.currentTimeMillis()/1000);
            }
        }
    }

    /**
     *  用户QQ邮箱注册
     * @param emailNum
     * @param emailCaptcha
     * @return
     */
    @Override
    public long userEmailRegister(String emailNum, String emailCaptcha) {
        //1.校验邮箱格式或者验证码是否正确
        if (!emailCodeValid(emailNum, emailCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式或邮箱验证码错误!!!");
        }
        //2.校验邮箱是否已经注册过
        synchronized (emailNum.intern()){
            //账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email",emailNum);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已经注册过了！！！账号重复");
            }
            //给用户分配调用接口的公钥和私钥ak,sk，保证复杂的同时要保证唯一
            String accessKey = DigestUtil.md5Hex(SALT + emailNum + RandomUtil.randomNumbers(4));
            String secretKey = DigestUtil.md5Hex(SALT + emailCaptcha + RandomUtil.randomNumbers(8));
            // 3. 插入数据
            User user = new User();
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setUserName(emailNum);
            user.setEmail(emailNum);
            boolean save = this.save(user);
            if (!save){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户账号密码登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 邮箱登录
     * @param emailNum
     * @param emailCode
     * @param request
     * @param response
     * @return
     */
    @Override
    public LoginUserVO userLoginBySms(String emailNum, String emailCode, HttpServletRequest request, HttpServletResponse response) {
        //1.校验邮箱验证码是否正确，包括是不是为空，是不是redis里面已经注册的邮箱和对应的验证码
        if (!emailCodeValid(emailNum, emailCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱验证码错误!!!");
        }
        //2.校验邮箱是否存在，获得对应邮箱的用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",emailNum);
        User user = this.getOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在！");
        }
        return setLoginUser(response, user);
    }


//    @Override
//    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
//        String unionId = wxOAuth2UserInfo.getUnionId();
//        String mpOpenId = wxOAuth2UserInfo.getOpenid();
//        // 单机锁
//        synchronized (unionId.intern()) {
//            // 查询用户是否已存在
//            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("unionId", unionId);
//            User user = this.getOne(queryWrapper);
//            // 被封号，禁止登录
//            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
//                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
//            }
//            // 用户不存在则创建
//            if (user == null) {
//                user = new User();
////                user.setUnionId(unionId);
////                user.setMpOpenId(mpOpenId);
//                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
//                user.setUserName(wxOAuth2UserInfo.getNickname());
//                boolean result = this.save(user);
//                if (!result) {
//                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
//                }
//            }
//            // 记录用户的登录态
//            request.getSession().setAttribute(USER_LOGIN_STATE, user);
//            return getLoginUserVO(user);
//        }
//    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取分页
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 更新用户
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        //允许用户修改自己的信息，但拒绝用户修改别人的信息，
        User loginUser = this.getLoginUser(request);
        Long id = userUpdateRequest.getId();
        if (id <= 0 ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户不存在");
        }
        if (!loginUser.getId().equals(id)){
            if (!loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

        //修改完要更新用户缓存
        loginUser.setUserName(userUpdateRequest.getUserName());
        loginUser.setGender(userUpdateRequest.getGender());
        String userJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + loginUser.getId(),userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 用户上传头像
     * @param file
     * @param request
     * @return
     */
    @Override
    public boolean uploadFileAvatar(MultipartFile file, HttpServletRequest request) {
        //获取登录用户信息
        User loginUser = this.getLoginUser(request);

        //更新持久层用户头像信息
        User user = new User();
        user.setId(loginUser.getId());
        String url = FileUploadUtil.uploadFileAvatar(file);
        user.setUserAvatar(url);
        boolean result = this.updateById(user);

        //更新用户缓存
        loginUser.setUserAvatar(url);
        String userjson = gson.toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + loginUser.getId(),userjson,JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return result;
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        //前端必须传一个 signature 来作为唯一标识
        String signature = request.getHeader("signature");
        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        try {
            // 自定义纯数字的验证码（随机4位数字，可重复）
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
            lineCaptcha.setGenerator(randomGenerator);
            //设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + signature, lineCaptcha.getCode(), 2, TimeUnit.MINUTES);
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 重新生成ak，sk，并重置用户ak，sk
     * @param request
     * @return
     */
    @Override
    public UserDevKeyVO genkey(HttpServletRequest request) {
        //获取当前登录对象
        User loginUser = getLoginUser(request);
        if (loginUser == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //获取当前登录用户的ak，sk
        UserDevKeyVO userDevKeyVO = usergenKey(loginUser.getUserAccount());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userAccount",loginUser.getUserAccount());
        updateWrapper.eq("id",loginUser.getId());
        updateWrapper.set("accessKey",userDevKeyVO.getAccessKey());
        updateWrapper.set("secretKey",userDevKeyVO.getSecretKey());
        this.update(updateWrapper);
        loginUser.setAccessKey(userDevKeyVO.getAccessKey());
        loginUser.setSecretKey(userDevKeyVO.getSecretKey());

        //重置登录用户的ak，sk信息
        String userJson = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + loginUser.getId(),userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return userDevKeyVO;
    }

    /**
     * 重新生成ak，sk的方法
     * @param userAccount
     * @return
     */
    private UserDevKeyVO usergenKey(String userAccount){
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(4));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        UserDevKeyVO userDevKeyVO = new UserDevKeyVO();
        userDevKeyVO.setAccessKey(accessKey);
        userDevKeyVO.setSecretKey(secretKey);
        return userDevKeyVO;
    }

    /**
     * 邮箱验证码校验
     * @param emailNum
     * @param emailCode
     * @return
     */
    private boolean emailCodeValid(String emailNum, String emailCode){
        //从redis里面获得对应的登录邮箱验证码
        String code = stringRedisTemplate.opsForValue().get(LOGINCODEPRE + emailNum);
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式或邮箱验证码错误!!!");
        }
        if (!emailCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式或邮箱验证码错误!!!");
        }
        return true;
    }

    /**
     * 邮箱验证码校验
     * @param emailNum
     * @param emailCode
     * @return
     */
    private boolean emailCodeValid1(String emailNum, String emailCode){
        //从redis里面获得对应的登录邮箱验证码
        String code = stringRedisTemplate.opsForValue().get(LOGINCODEPRE + emailNum);
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式或邮箱验证码错误!!!");
        }
        if (!emailCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式或邮箱验证码错误!!!");
        }
        return true;
    }

    /**
     * 记录用户的登录态，并返回脱敏后的登录用户
     * @param response
     * @param user
     * @return
     */
    private LoginUserVO setLoginUser(HttpServletResponse response, User user) {
        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        response.addCookie(cookie);
        String userJson = gson.toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + user.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getLoginUserVO(user);
    }
}
