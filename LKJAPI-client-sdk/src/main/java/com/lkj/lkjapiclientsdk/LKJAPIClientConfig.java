package com.lkj.lkjapiclientsdk;

import com.lkj.lkjapiclientsdk.client.NameApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: LKJAPI 客户端配置
 * @Author：LKJ
 * @Package：com.lkj.lkjapiclientsdk
 * @Project：LKJAPI-client-sdk
 * @name：LKJAPIClientConfig
 * @Date：2023/12/5 16:32
 * @Filename：LKJAPIClientConfig
 */
//Configuration注解，将该类标记为一个配置类，告诉spring这是一个用于配置的类
@Configuration
//读取application.yml的配置，读取到配置后，把这个配置设置到我们的属性中
//这里给所有的配置加上前缀“lkjapi.client”
@ConfigurationProperties("lkjapi.client")
@ComponentScan
@Data
//自动扫描组件，使得spring能够自动注入相应的Bean
@ComponentScan
public class LKJAPIClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public NameApiClient lkjapiClient(){

        return new NameApiClient(accessKey,secretKey);
    }
}
