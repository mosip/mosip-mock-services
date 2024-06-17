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

/**
 * Main application class for Proxy ABIS.
 * <p>
 * This class initializes and runs the Proxy ABIS application using Spring Boot.
 * It scans the necessary packages, configures JPA repositories, and enables JMS
 * messaging. On startup, it initializes the necessary components and starts
 * listening to ABIS queues.
 * </p>
 */
@ComponentScan(basePackages = { "io.mosip.proxy.abis", "${mosip.auth.adapter.impl.basepackage}" })
@EntityScan(basePackages = { "io.mosip.proxy.abis.*" })
@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = { "io.mosip.proxy.abis.*" })
@EnableJms
public class ProxyAbisApplication {

	/**
	 * Main method to start the Proxy ABIS application.
	 * <p>
	 * It initializes the Spring application context, starts the ABIS listener, and
	 * sets up the controller to use the listener.
	 * </p>
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication
				.run(ProxyAbisApplication.class, args);
		configurableApplicationContext.getBean(Listener.class).runAbisQueue();
		configurableApplicationContext.getBean(ProxyAbisController.class)
				.setListener(configurableApplicationContext.getBean(Listener.class));
	}
}