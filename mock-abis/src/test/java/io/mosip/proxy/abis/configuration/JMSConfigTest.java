package io.mosip.proxy.abis.configuration;

import io.mosip.proxy.abis.exception.AbisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import io.mosip.proxy.abis.utility.Helpers;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Test class for JMSConfig.
 * Tests the configuration and behavior of ActiveMQ connection factory and related JSON handling.
 * Validates the ABIS queue configuration settings and JSON processing functionality.
 */
class JMSConfigTest {

    /** The JMSConfig instance being tested */
    private JMSConfig jmsConfig;

    /** Mock RestTemplate for testing HTTP requests */
    private RestTemplate mockRestTemplate;

    /** Valid JSON string representing ABIS configuration */
    private static final String VALID_JSON = "{\"abis\":[{\"userName\":\"testUser\",\"password\":\"testPass\",\"brokerUrl\":\"tcp://localhost:61616\"}]}";

    /**
     * Sets up the test environment before each test.
     * Initializes JMSConfig instance and mocks, and sets default configuration values.
     * Uses ReflectionTestUtils to set private fields normally set by @Value annotations.
     */
    @BeforeEach
    void setUp() {
        jmsConfig = new JMSConfig();
        mockRestTemplate = mock(RestTemplate.class);

        // Set mock values to fields with @Value annotations
        ReflectionTestUtils.setField(jmsConfig, "configServerFileStorageURL", "http://mock-config/");
        ReflectionTestUtils.setField(jmsConfig, "registrationProcessorAbisJson", "/mock.json");
        ReflectionTestUtils.setField(jmsConfig, "localDevelopment", false);
    }

    /**
     * Tests ActiveMQ connection factory creation in local development mode.
     * Verifies that:
     * - The factory is created successfully
     * - The factory has the correct username configured
     * Uses MockedStatic to mock static Helpers class methods
     */
    @Test
    void testActiveMQConnectionFactory_LocalDevelopment_ReturnsValidFactory() {
        try (MockedStatic<Helpers> mockedHelpers = Mockito.mockStatic(Helpers.class)) {
            ReflectionTestUtils.setField(jmsConfig, "localDevelopment", true);
            mockedHelpers.when(() -> Helpers.readFileFromResources(anyString())).thenReturn(VALID_JSON);
            ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();
            assertNotNull(factory);
            assertEquals("testUser", factory.getUserName());
        }
    }

    /**
     * Tests JSON retrieval functionality in local development mode.
     * Verifies that the correct JSON is read from resources when running locally.
     * Uses MockedStatic to simulate file reading from resources.
     */
    @Test
    void testGetJson_LocalDevelopment_ReturnsValidJson() throws Exception {
        try (MockedStatic<Helpers> mockedHelpers = Mockito.mockStatic(Helpers.class)) {
            mockedHelpers.when(() -> Helpers.readFileFromResources(anyString())).thenReturn(VALID_JSON);
            String result = ReflectionTestUtils.invokeMethod(jmsConfig, "getJson",
                    "http://mock-config/", "/mock.json", true);
            assertEquals(VALID_JSON, result);
        }
    }

    /**
     * Tests successful validation and extraction of values from ABIS queue JSON.
     * Verifies that the correct value is returned when the requested key exists in the JSON map.
     * Uses ReflectionTestUtils to access private method.
     */
    @Test
    void testvalidateAbisQueueJsonAndReturnValue_WhenKeyExists_ReturnsValue() {
        Map<String, String> jsonMap = Map.of("userName", "testUser");
        String result = ReflectionTestUtils.invokeMethod(jmsConfig,
                "validateAbisQueueJsonAndReturnValue", jsonMap, "userName");
        assertEquals("testUser", result);
    }

    /**
     * Tests handling of empty ABIS array in configuration.
     * Verifies that null is returned when the ABIS array in JSON is empty.
     * Uses MockedStatic to simulate empty JSON response.
     */
    @Test
    void testActiveMQConnectionFactory_EmptyAbisArray_ReturnsNull() {
        try (MockedStatic<Helpers> mockedHelpers = Mockito.mockStatic(Helpers.class)) {
            String emptyJson = "{\"abis\":[]}";
            mockedHelpers.when(() -> Helpers.readFileFromResources(anyString())).thenReturn(emptyJson);
            ActiveMQConnectionFactory factory = jmsConfig.activeMQConnectionFactory();
            assertNull(factory);
        }
    }

    /**
     * Tests validation behavior when a required key is missing from the JSON.
     * Verifies that an AbisException is thrown with appropriate error message
     * when attempting to retrieve a non-existent key.
     * Uses ReflectionTestUtils to access private method.
     */
    @Test
    void testValidateAbisQueueJsonAndReturnValue_missingKey_shouldThrowAbisException() {
        Map<String, String> jsonMap = Map.of("brokerUrl", "tcp://localhost:61616");
        AbisException thrown = assertThrows(AbisException.class,
                () -> ReflectionTestUtils.invokeMethod(jmsConfig, "validateAbisQueueJsonAndReturnValue", jsonMap, "userName"));
        assertTrue(thrown.getMessage().contains("userName"));
    }
}