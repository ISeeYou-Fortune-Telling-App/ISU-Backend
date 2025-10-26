package com.iseeyou.fortunetelling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FortunetellingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FortunetellingApplication.class, args);
    }

}
