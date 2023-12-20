package com.lkj.springbootinit.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI-backend
 * @name：UserInterfaceInfoServiceTest
 * @Date：2023/12/9 16:02
 * @Filename：UserInterfaceInfoServiceTest
 */
@SpringBootTest
public class UserInterfaceInfoServiceTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Test
    public void invokeCount() {

        boolean b = userInterfaceInfoService.invokeCount(1, 1);
        Assertions.assertTrue(b);
    }
}