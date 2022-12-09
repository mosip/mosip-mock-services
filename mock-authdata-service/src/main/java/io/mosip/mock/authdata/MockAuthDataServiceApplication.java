package io.mosip.mock.authdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

@ComponentScan(basePackages = { "io.mosip.mock.authdata" })
@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "io.mosip.proxy.abis.dao" })
@EnableJms
public class MockAuthDataServiceApplication 
{
	public static void main(String[] args) {
		SpringApplication.run(MockAuthDataServiceApplication.class, args);
	}
}
