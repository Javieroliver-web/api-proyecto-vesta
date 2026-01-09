package com.vesta.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VestaApiApplication {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VestaApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VestaApiApplication.class, args);
        logger.info("🚀 API Vesta iniciada en el puerto 8080");
    }
}