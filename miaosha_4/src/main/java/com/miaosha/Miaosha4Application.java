package com.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Miaosha4Application{

    public static void main(String[] args) {
        SpringApplication.run(Miaosha4Application.class, args);
    }

    //以War包形式部署
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        //Application的类名
//        return application.sources(Miaosha4Application.class);
//    }

}
