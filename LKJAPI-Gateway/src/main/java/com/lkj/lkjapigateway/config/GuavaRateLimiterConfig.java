package com.lkj.lkjapigateway.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuavaRateLimiterConfig {


    @SuppressWarnings("UnstableApiUsage")
    @Bean
    public RateLimiter rateLimiter(){
        //每秒放置20个令牌，即50ms向令牌桶放置一个令牌
        return RateLimiter.create(20);
    }
}