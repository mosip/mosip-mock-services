package io.mosip.proxy.abis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = { "io.mosip.proxy.abis" })
@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "io.mosip.proxy.abis.dao" })
@EnableJms
public class ProxyAbisApplication {
	public static void main(String[] args)  {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(ProxyAbisApplication.class, args);
		configurableApplicationContext.getBean(Listener.class).runAbisQueue();
	}
		
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}