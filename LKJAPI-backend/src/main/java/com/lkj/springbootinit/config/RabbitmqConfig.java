package com.lkj.springbootinit.config;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.lkj.apicommon.constant.RabbitmqConstant.*;


/**
 * 1.声明异步发送短信所需要用到的交换机和队列
 * channel 消息通道: 在客户端的每个连接里，可建立多个channel，每个channel代表一个会话任务。
 * exchange 交换机:exchange的功能是用于消息分发，它负责接收消息并转发到与之绑定的队列，exchange不存储消息，
 * 如果一个exchange没有binding任何Queue，那么当它会丢弃生产者发送过来的消息，在启用ACK机制后，如果exchange找不到队列，则会返回错误。
 * 一个exchange可以和多个Queue进行绑定。

 */
@Configuration
@Slf4j
public class RabbitmqConfig {


    //声明交换机 exchange_sms_inform
    @Bean(EXCHANGE_SMS_INFORM)
    public Exchange EXCHANGE_SMS_INFORM(){
        return new DirectExchange(EXCHANGE_SMS_INFORM,true,false);
    }

    //声明QUEUE_LOGIN_SMS队列 queue_sms_code
    @Bean(QUEUE_LOGIN_SMS)
    public Queue QUEUE_INTERFACE_SMS(){
        return new Queue(QUEUE_LOGIN_SMS,true,false,false);
    }

    //创建交换机，交换机绑定队列
    @Bean
    public Binding BINDING_QUEUE_LOGIN_SMS(){
        return new Binding(QUEUE_LOGIN_SMS,
                Binding.DestinationType.QUEUE, EXCHANGE_SMS_INFORM,
                ROUTINGKEY_SMS,null);
    }
}