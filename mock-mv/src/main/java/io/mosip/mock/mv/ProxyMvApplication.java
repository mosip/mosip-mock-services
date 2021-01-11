package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = { "io.mosip." })
@SpringBootApplication
@EnableAutoConfiguration
@EnableJms
public class ProxyMvApplication {
	public static void main(String[] args)  {
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ProxyMvApplication.class, args);
		configurableApplcnConetxt.getBean(Listener.class).runMvQueue();
		}


	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}