package com.core.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.core")
@EnableJpaRepositories(basePackages = "com.core.webhook")
@EnableFeignClients
@EnableScheduling
public class CoreWebhookApplication {

	public static void main(String[] args) {
        System.setProperty("spring.config.location", "config/");
        SpringApplication.run(CoreWebhookApplication.class, args);
	}

}
