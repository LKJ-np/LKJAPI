package com.lkj.springbootinit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig{

    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
//        //创建RedisTemplate对象
//        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
//        //设置连接工厂
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        //设置Key的序列化
//        redisTemplate.setKeySerializer(RedisSerializer.string());
//        //创建Json序列化工具
//        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
//        //设置Value的序列化
//        redisTemplate.setValueSerializer(jsonRedisSerializer);
//        log.info("自定义redis序列化....");
//        return redisTemplate;
        RedisTemplate<Object,Object> redisTemplate=new RedisTemplate<Object,Object>();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setStringSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}