package com.company.promo.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.company.promo.service",
        "com.company.promobridge"
})
public class PromoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromoServiceApplication.class, args);
    }
}

