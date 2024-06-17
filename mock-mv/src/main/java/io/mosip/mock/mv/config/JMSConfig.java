package io.mosip.mock.mv.config;

import java.util.Arrays;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up JMS (Java Message Service) connection
 * factory.
 * <p>
 * This class configures the ActiveMQ connection factory with specified
 * username, password, and broker URL.
 */
@Configuration
public class JMSConfig {
	private static final Logger logger = LoggerFactory.getLogger(JMSConfig.class);

	/**
	 * The Constant FAIL_OVER used for configuring the failover URL for ActiveMQ.
	 */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE used to disable URL randomization. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	/** The username for accessing the JMS queue. */
	@Value("${registration.processor.manual.adjudication.queue.username}")
	private String mausername;

	/** The password for accessing the JMS queue. */
	@Value("${registration.processor.manual.adjudication.queue.password}")
	private String mapassword;

	/** The URL of the JMS broker for manual adjudication queue. */
	@Value("${registration.processor.manual.adjudication.queue.url}")
	private String mabrokerUrl;

	/**
	 * Bean definition for creating an ActiveMQ connection factory.
	 * <p>
	 * Configures the ActiveMQ connection factory with failover URL, trusted
	 * packages, username, and password for connecting to the JMS broker.
	 *
	 * @return ActiveMQConnectionFactory configured with the specified properties.
	 */
	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		logger.info("Creating new connection from configuration.");
		String failOverBrokerUrl = FAIL_OVER + mabrokerUrl + "," + mabrokerUrl + RANDOMIZE_FALSE;
		logger.info("Broker url : {}", failOverBrokerUrl);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(failOverBrokerUrl);
		factory.setTrustedPackages(Arrays.asList("io.mosip.mock.mv.*"));
		factory.setUserName(mausername);
		factory.setPassword(mapassword);
		return factory;
	}
}