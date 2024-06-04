package io.mosip.mock.mv.config;

import java.util.Arrays;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JMSConfig {
	private static final Logger logger = LoggerFactory.getLogger(JMSConfig.class);

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	/** The username. */
	@Value("${registration.processor.manual.adjudication.queue.username}")
	private String mausername;

	/** The password. */
	@Value("${registration.processor.manual.adjudication.queue.password}")
	private String mapassword;

	/** The URL. */
	@Value("${registration.processor.manual.adjudication.queue.url}")
	private String mabrokerUrl;

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
