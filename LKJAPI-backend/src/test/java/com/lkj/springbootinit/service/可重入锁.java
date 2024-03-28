package com.lkj.springbootinit.service;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：可重入锁
 * @Date：2024/3/19 14:19
 * @Filename：可重入锁
 */
public class 可重入锁 {
    public synchronized void method1(){
        System.out.println("方法一");
        System.out.println(Thread.currentThread().getId());
        method2();
    }
    public synchronized void method2(){
        System.out.println(Thread.currentThread().getId());
        System.out.println("方法二");
        System.out.println(Thread.currentThread().getId());
    }

    public static void main(String[] args) {
        可重入锁 k = new 可重入锁();
        k.method1();
    }
}
