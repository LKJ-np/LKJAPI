package com.lkj.springbootinit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 测试volatile的可见性
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test
 * @Project：LKJAPI
 * @name：Test
 * @Date：2024/3/28 8:39
 * @Filename：Test
 */
public class Test {
    static  boolean flag = false;
    public static void main(String[] args) {
        //启用一个线程，如果主线程修改该布尔值为true则退出循环，如果线程中对于修改的值不可间，那么程序会一直在循环中
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "线程启动，falg为" + flag);
            while (!flag) {

            }
            System.out.println(Thread.currentThread().getName() + "退出循环");
        },"t1").start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //main线程把布尔值修改为true
        flag = true;
        System.out.println(Thread.currentThread().getName()+"修改flag为" +flag);
    }
}
