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

	/** The username. */
	@Value("${registration.processor.verification.queue.username}")
	private String vusername;

	/** The password. */
	@Value("${registration.processor.verification.queue.password}")
	private String vpassword;

	/** The URL. */
	@Value("${registration.processor.verification.queue.url}")
	private String vbrokerUrl;

	@Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(vbrokerUrl);
        factory.setUserName(vusername);
        factory.setPassword(vbrokerUrl);
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
