package com.lkj.lkjapiinterface.service;

import com.lkj.lkjapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.lkjapiinterface
 * @Project：LKJAPI-interface
 * @name：testHutoll
 * @Date：2023/12/5 13:10
 * @Filename：testHutoll
 */
@SpringBootTest
public class testHutool {
// 未写sdk之前的测试
//    @Test
//    public static void main(String[] args) {
//
//         String accessKey ="lkj";
//
//         String secretKey ="abcdefg";
//
//        HutoolConnection hutoolConnection =new HutoolConnection(accessKey, secretKey);
//
////        String lkj_get = hutoolConnection.getNameByGet("lkj Get");
////        System.out.println(lkj_get);
////        System.out.println("***************");
////
////        String lkj_post = hutoolConnection.getNameByPost("lkj post");
////        System.out.println(lkj_post);
////        System.out.println("***************");
//
//        User user = new User();
//        //todo 需要解决如果setusername为中文的时候，为什么报错
//        user.setUsername("lkj user");
//        String usernameByPost = hutoolConnection.getUsernameByPost(user);
//        System.out.println(usernameByPost);
//    }

    @Resource
    private LKJAPIClient lkjapiClient;

    //写了sdk后的测试
    @Test
   void test() {
        //第一个方法，get方法
        String byGet = lkjapiClient.getNameByGet("lkj get");
        System.out.println(byGet);
        System.out.println("*******1*****");
        //第二个方法，post方法
        String lkj_post = lkjapiClient.getNameByPost("lkj post");
        System.out.println(lkj_post);
        System.out.println("*******2*****");
        //第三个方法，post by 对象
        User user = new User();
        user.setUsername("lkj");
        String usernameByPost = lkjapiClient.getUserNameByPost(user);
        System.out.println(usernameByPost);
        System.out.println("*******3*****");
    }
}
