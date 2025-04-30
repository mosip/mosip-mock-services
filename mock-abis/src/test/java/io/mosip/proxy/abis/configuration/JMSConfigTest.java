package io.mosip.proxy.abis.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Test class for JMSConfig to verify the configuration of JMS-related beans.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "config.server.file.storage.uri=http://dummy",
        "registration.processor.abis.json=/dummy",
        "local.development=true"
})
class JMSConfigTest {

    /**
     * Mocked RestTemplate bean with the qualifier "selfTokenRestTemplate".
     * This is used to ensure the application context loads without requiring
     * the actual RestTemplate bean.
     */
    @MockBean(name = "selfTokenRestTemplate")
    private RestTemplate restTemplate;

    /**
     * Autowired instance of JMSConfig to test its bean creation methods.
     */
    @Autowired
    private JMSConfig jmsConfig;

    /**
     * Test to verify that the ActiveMQConnectionFactory bean is created successfully.
     * This ensures that the JMS configuration is correctly set up.
     */
    @Test
    void testActiveMQConnectionFactory() {
        ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();
        assertNotNull(factory, "The ActiveMQConnectionFactory bean should be created");
    }
}