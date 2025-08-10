package com.yusufu.javaspringfeatures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TradingAdvisorApp {

    public static void main(String[] args) {
        SpringApplication.run(TradingAdvisorApp.class, args);
    }

}
