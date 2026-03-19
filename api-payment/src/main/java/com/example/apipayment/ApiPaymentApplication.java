package com.example.apipayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ApiPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiPaymentApplication.class, args);
    }

}
