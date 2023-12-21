package com.lkj.springioc;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springioc
 * @Project：LKJAPI
 * @name：MessageServiceImpl
 * @Date：2023/12/21 10:44
 * @Filename：MessageServiceImpl
 */
public class MessageServiceImpl implements MessageService{
    @Override
    public String getMessage() {

        return "hello ,word";
    }
}
