package com.dongnguyen248.add2num.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Add2NumWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(Add2NumWebApplication.class, args);
    }
}