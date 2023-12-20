package com.lkj.springbootinit.provider;


import com.lkj.springbootinit.service.UserService;
import com.lkj.apicommon.entity.User;
import com.lkj.apicommon.service.InnerUserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getUserById(Long userId) {
        return userService.getById(userId);
    }
}
