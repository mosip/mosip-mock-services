package io.mosip.mock.mv.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for the JMSConfig class to verify that the ActiveMQConnectionFactory
 * is configured properly with the provided values.
 */
class JMSConfigTest {

    private JMSConfig jmsConfig;

    /**
     * Set up the test environment before each test method.
     * It initializes the JMSConfig instance and injects mock values
     * for the properties using ReflectionTestUtils (simulating @Value injection).
     */
    @BeforeEach
    void setUp() {
        jmsConfig = new JMSConfig();
        // Set default values to simulate injected properties
        ReflectionTestUtils.setField(jmsConfig, "mausername", "testuser");
        ReflectionTestUtils.setField(jmsConfig, "mapassword", "testpass");
        ReflectionTestUtils.setField(jmsConfig, "mabrokerUrl", "tcp://localhost:61616");
    }

    /**
     * Tests that the ActiveMQConnectionFactory is created with the expected
     * default property values.
     * Verifies broker URL, trusted packages, username, and password.
     */
    @Test
    void activeMQConnectionFactory_ConfiguresCorrectly() {
        // Act: Call the method under test
        ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();

        // Assert: Validate the configuration
        assertNotNull(factory, "ActiveMQConnectionFactory should not be null");
        assertEquals("failover:(tcp://localhost:61616,tcp://localhost:61616)?randomize=false",
                factory.getBrokerURL(), "Broker URL should match the failover configuration");
        assertEquals(List.of("io.mosip.mock.mv.*"), factory.getTrustedPackages(),
                "Trusted packages should be set correctly");
        assertEquals("testuser", factory.getUserName(), "Username should match the configured value");
        assertEquals("testpass", factory.getPassword(), "Password should match the configured value");
    }

    /**
     * Tests that the ActiveMQConnectionFactory is correctly reconfigured
     * when different property values are set.
     * Ensures that the factory reflects the updated username, password, and broker URL.
     */
    @Test
    void activeMQConnectionFactory_WithDifferentProperties_ConfiguresCorrectly() {
        // Arrange: Set different property values
        ReflectionTestUtils.setField(jmsConfig, "mausername", "newuser");
        ReflectionTestUtils.setField(jmsConfig, "mapassword", "newpass");
        ReflectionTestUtils.setField(jmsConfig, "mabrokerUrl", "tcp://newhost:61617");

        // Act: Call the method under test
        ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();

        // Assert: Validate the reconfigured factory
        assertNotNull(factory, "ActiveMQConnectionFactory should not be null");
        assertEquals("failover:(tcp://newhost:61617,tcp://newhost:61617)?randomize=false",
                factory.getBrokerURL(), "Broker URL should match the new failover configuration");
        assertEquals(List.of("io.mosip.mock.mv.*"), factory.getTrustedPackages(),
                "Trusted packages should be set correctly");
        assertEquals("newuser", factory.getUserName(), "Username should match the new value");
        assertEquals("newpass", factory.getPassword(), "Password should match the new value");
    }
}
