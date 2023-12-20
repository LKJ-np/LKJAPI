package com.lkj.apithirdpartyservices.listener;

import com.lkj.apithirdpartyservices.utils.SendMessageOperation;
import com.lkj.apicommon.entity.SmsMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.lkj.apicommon.constant.RabbitmqConstant.QUEUE_LOGIN_SMS;


@Component
@Slf4j
public class MessageListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    //监听queue_sms_code队列，实现接口统计功能
    //生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    //直接使用@RabbitListener(queues = "myQueue")  不能自动创建队列
    //自动创建队列 @RabbitListener(queuesToDeclare = @Queue("myQueue"))
    @RabbitListener(queuesToDeclare = { @Queue(QUEUE_LOGIN_SMS)})
    public void receiveSms(SmsMessage smsMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息啦，内容是："+smsMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

            //发送邮箱验证码
            SendMessageOperation messageOperation = new SendMessageOperation();
            String targetEmail = smsMessage.getEmail();
            messageOperation.sendMessage(targetEmail,stringRedisTemplate);

//        try {
//            log.info("监听到消息啦，内容是："+smsMessage);
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//
//            //发送邮箱验证码
//            SendMessageOperation messageOperation = new SendMessageOperation();
//            String targetEmail = smsMessage.getEmail();
//            messageOperation.sendMessage(targetEmail,stringRedisTemplate);
//        } catch (IOException e) {
//            log.error("邮箱发送消息失败【】error："+ message.getBody());
//            log.error("Email failed to send message  handleMessage {} , error:",message,e);
//            //处理消息失败，将消息重新放回队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
//
//        }

    }


}