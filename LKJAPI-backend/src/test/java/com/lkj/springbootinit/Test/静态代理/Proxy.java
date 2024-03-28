package com.lkj.springbootinit.Test.静态代理;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test.静态代理
 * @Project：LKJAPI
 * @name：Proxy
 * @Date：2024/3/28 8:55
 * @Filename：Proxy
 */
public class Proxy implements rent{
    Host host;

    public void setHost(Host host){
        this.host = host;
    }

    @Override
    public void rent() {
        host.rent();
    }
}
