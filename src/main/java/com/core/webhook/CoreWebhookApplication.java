package com.core.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.core")
@EnableJpaRepositories(basePackages = "com.core.webhook")
public class CoreWebhookApplication {

	public static void main(String[] args) {
        System.setProperty("spring.config.location", "config/");
        SpringApplication.run(CoreWebhookApplication.class, args);
	}

}
