package io.mosip.mock.mv;

import io.mosip.mock.mv.queue.Listener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

/**
 * Main class for the Proxy MV (Matching Verification) Application.
 * <p>
 * This class is responsible for bootstrapping the Spring Boot application and
 * initializing required components such as JMS listeners and REST templates.
 * </p>
 */
@ComponentScan(basePackages = { "io.mosip.*" })
@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, JmsAutoConfiguration.class })
@EnableJms
public class ProxyMvApplication {

	/**
	 * The entry point of the Spring Boot application.
	 * <p>
	 * This method is responsible for launching the Spring Boot application,
	 * retrieving the {@link Listener} bean, and starting the adjudication and
	 * verification queues.
	 * </p>
	 *
	 * @param args command-line arguments (not used).
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ProxyMvApplication.class,
				args);
		Listener listener = configurableApplcnConetxt.getBean(Listener.class);
		listener.runAdjudicationQueue();
		listener.runVerificationQueue();
	}

	/**
	 * Creates a {@link RestTemplate} bean.
	 * <p>
	 * This method provides a {@link RestTemplate} instance that can be used for
	 * making RESTful web service calls.
	 * </p>
	 *
	 * @return a {@link RestTemplate} instance.
	 */
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}