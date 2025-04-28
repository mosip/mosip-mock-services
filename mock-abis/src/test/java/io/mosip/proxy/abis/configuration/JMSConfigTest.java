// File: src/test/java/io/mosip/proxy/abis/configuration/JMSConfigTest.java
package io.mosip.proxy.abis.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestPropertySource(properties = {
        "config.server.file.storage.uri=http://dummy",
        "registration.processor.abis.json=/dummy",
        "local.development=true"
})
class JMSConfigTest {

    @MockBean(name = "selfTokenRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private JMSConfig jmsConfig;

    @Test
    void testActiveMQConnectionFactory() {
        ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();
        assertNotNull(factory, "The ActiveMQConnectionFactory bean should be created");
    }
}