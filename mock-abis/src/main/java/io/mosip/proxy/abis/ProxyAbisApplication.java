package io.mosip.proxy.abis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.listener.Listener;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@ComponentScan(basePackages = { "io.mosip.proxy.abis", "${mosip.auth.adapter.impl.basepackage}" })
@EntityScan(basePackages = { "io.mosip.proxy.abis.*"})
@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "io.mosip.proxy.abis.*" })
@EnableJms
public class ProxyAbisApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication
				.run(ProxyAbisApplication.class, args);
		configurableApplicationContext.getBean(Listener.class).runAbisQueue();
		configurableApplicationContext.getBean(ProxyAbisController.class).setListener(configurableApplicationContext.getBean(Listener.class));		
	}
}