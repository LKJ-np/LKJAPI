package com.lkj.springbootinit.service;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：死锁
 * @Date：2024/3/20 10:49
 * @Filename：死锁
 */
public class 死锁 {
    public static void main(String[] args) {
        Object o1 = new Object();
        Object o2 = new Object();
        dowork(o1,o2,"线程1");
        dowork(o2,o1,"线程2");
    }

    private static void dowork(Object p, Object q,String s) {
        new Thread(()->{
            synchronized (p){
                //业务代码
                System.out.println(p);
                System.out.println(s);
                synchronized (q){
                    System.out.println(q);
                    System.out.println(s);
                }
            }
        }).start();
    }
}
