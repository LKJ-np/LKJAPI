package com.lkj.springbootinit.Test.静态代理;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test.静态代理
 * @Project：LKJAPI
 * @name：Host
 * @Date：2024/3/28 8:54
 * @Filename：Host
 */
public class Host implements rent{
    @Override
    public void rent() {
        System.out.println("我想出租房子");
    }
}
