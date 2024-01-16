package com.lkj.springbootinit.service;

import com.lkj.apicommon.entity.User;
import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 线程池
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：ExecutorJob
 * @Date：2024/1/16 15:37
 * @Filename：ExecutorJob
 */
public class ExecutorJob {

    @Resource
    UserService userService;

    //使用多线程插入用户信息
    @Test
    public void InsertUser(){
        //自定义线程池
        //corePoolSize 线程池的核心线程数量
        //maximumPoolSize 线程池的最大线程数
        // keepAliveTime 当线程数大于核心线程数时，多余的空闲线程存活的最长时间
        // TimeUnit unit 时间单位
        // BlockingQueue<Runnable> workQueue 任务队列，用来储存等待执行任务的队列
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20,60,1000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int j  =0;
        int batchsize= 25000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUserName("假lkj");
                user.setUserAccount("假lkj");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("lkj@qq.com");
                userList.add(user);
                if (j % batchsize == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
                System.out.println("ThreadName:" + Thread.currentThread().getName());
                userService.saveBatch(userList,batchsize);
            },executor);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}
