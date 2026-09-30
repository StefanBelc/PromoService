package com.company.promo.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages (instead of a separate @ComponentScan) keeps Spring Boot's test-slice
// filters, so @WebMvcTest loads only the web layer rather than every bean, including Kafka and Redis.
@SpringBootApplication(scanBasePackages = {
        "com.company.promo.service",
        "com.company.promobridge"
})
public class PromoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromoServiceApplication.class, args);
    }
}
