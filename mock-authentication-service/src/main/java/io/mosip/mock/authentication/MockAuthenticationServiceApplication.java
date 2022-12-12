package io.mosip.mock.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "io.mosip.mock.authentication" })
public class MockAuthenticationServiceApplication 
{
	public static void main(String[] args) {
		SpringApplication.run(MockAuthenticationServiceApplication.class, args);
	}
}
