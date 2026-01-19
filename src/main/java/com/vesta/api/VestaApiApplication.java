package com.vesta.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableScheduling
public class VestaApiApplication extends SpringBootServletInitializer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VestaApiApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VestaApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(VestaApiApplication.class, args);
        logger.info("🚀 API Vesta iniciada en el puerto 8080");
    }
}