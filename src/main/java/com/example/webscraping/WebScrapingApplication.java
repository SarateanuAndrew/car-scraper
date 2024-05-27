package com.example.webscraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebScrapingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebScrapingApplication.class, args);
    }
}
