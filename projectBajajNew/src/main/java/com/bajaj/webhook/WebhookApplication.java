package com.bajaj.webhook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebhookApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebhookApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(StartupService startupService) {
        return args -> startupService.executeStartupFlow();
    }
}
