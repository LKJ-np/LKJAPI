package com.lkj.springioc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class SpringIocApplication {

    public static void main(String[] args) {

//        SpringApplication.run(SpringIocApplication.class, args);
        // 用我们的配置文件来启动一个 ApplicationContext
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:application.xml");
        System.out.println("context 启动成功");

        //从cintext中取出我们的bean，而不是用 new MessageServiceImpl（）这种方式
        MessageService messageService = context.getBean(MessageService.class);
        //这句将输出：hello world
        System.out.println(messageService.getMessage());
    }

}
