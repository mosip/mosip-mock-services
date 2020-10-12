package com.proxy.abis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = { "com.proxy.abis" })
@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "com.proxy.abis.dao" })
@EnableJms
public class ProxyAbisApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProxyAbisApplication.class, args);

	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}