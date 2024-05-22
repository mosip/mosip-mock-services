package io.mosip.mock.mv.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		logger.info(String.format("Broker url : %s", failOverBrokerUrl));
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(failOverBrokerUrl);
        factory.setUserName(mausername);
        factory.setPassword(mapassword);
        return factory;
    }

	/*
	@Bean
    public JmsTemplate jmsTemplate(ActiveMQConnectionFactory activeMQConnectionFactory) {
        return new JmsTemplate(new SingleConnectionFactory(activeMQConnectionFactory));
    }

    @Bean
    public HealthIndicator jmsHealthIndicator(JmsTemplate jmsTemplate) {
        return () -> {
            try {
                jmsTemplate.execute(session -> null);
                return Health.up().build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
    */
}
