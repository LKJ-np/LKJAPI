package com.lkj.springbootinit.Test.静态代理;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test.静态代理
 * @Project：LKJAPI
 * @name：Client
 * @Date：2024/3/28 8:56
 * @Filename：Client
 */
public class Client {
    public static void main(String[] args) {
        Host host =new Host(); //这里可以用多态 来实例化不同的房东
        Proxy proxy = new Proxy();
        proxy.setHost(host); //这样可以通过传入不同房东 来实现不同的代理租房
        host.rent();

    }
}
