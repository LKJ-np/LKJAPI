package com.lkj.springbootinit.service;

/**
 * @Description:双检锁单例
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：SingleInstance
 * @Date：2024/3/18 20:37
 * @Filename：SingleInstance
 */
public class SingleInstance {
    //必须有volatile修饰（防止指令重排序）
    private volatile static SingleInstance instance;

    //构造函数必须私有（防止外部通过构造方法创建对象）
    private SingleInstance(){}

    public static SingleInstance getInstance(){
        //第一个判空（如果不是空，就不必再进入同步代码块了，提高效率）
        if (instance == null){
            //这里加锁，是为了防止多线程的情况下出现实例化多个对象的情况
            synchronized (SingleInstance.class){
                //第二个判空（如果是空，就实例化对象）
                if (instance == null){
                    //新建实例
                    instance = new SingleInstance();
                }
            }
        }
        return instance;
    }
}
