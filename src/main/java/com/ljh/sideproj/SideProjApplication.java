package com.ljh.sideproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SideProjApplication {

    public static void main (String[] args) {
        SpringApplication.run(SideProjApplication.class, args);
    }

}
