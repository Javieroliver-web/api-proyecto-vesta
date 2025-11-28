package com.vesta.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VestaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VestaApiApplication.class, args);
        System.out.println("🚀 API Vesta iniciada en el puerto 8080");
    }
}