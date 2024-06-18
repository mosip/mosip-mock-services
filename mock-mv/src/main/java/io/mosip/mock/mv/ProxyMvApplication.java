package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = { "io.mosip.*" })
@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@EnableJms
public class ProxyMvApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ProxyMvApplication.class,
				args);
		Listener listener = configurableApplcnConetxt.getBean(Listener.class);
		listener.runAdjudicationQueue();
		listener.runVerificationQueue();
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}