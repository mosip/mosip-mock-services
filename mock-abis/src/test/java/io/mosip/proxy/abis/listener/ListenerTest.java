package io.mosip.proxy.abis.listener;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for Listener.
 * This class tests various methods of the Listener class, including validation,
 * message consumption, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
public class ListenerTest {

    @InjectMocks
    private Listener listener; // Instance of Listener with mocks injected

    @Mock
    private ProxyAbisController proxyAbisController;

    /**
     * Sets up the test environment before each test.
     * Initializes mocks and injects them into the Listener instance.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the isValidInsertRequestDto method with only standard keys.
     * Verifies that the method returns false.
     */
    @Test
    void testIsValidInsertRequestDto_withOnlyStandardKeys_shouldReturnFalse() {
        Map<String, String> validMap = new HashMap<>();
        validMap.put("id", "mosip.abis.insert");
        validMap.put("version", "1.1");
        validMap.put("requestId", "123");
        validMap.put("requesttime", "2024-04-22T10:00:00Z");
        validMap.put("referenceId", "ref-123");
        validMap.put("referenceURL", "http://example.com");

        boolean result = Listener.isValidInsertRequestDto(validMap);
        assertFalse(result); // Verify the result is false
    }

    /**
     * Tests the isValidIdentifyRequestDto method with valid keys only.
     * Verifies that the method returns false.
     */
    @Test
    void testIsValidIdentifyRequestDto_withValidKeysOnly_shouldReturnFalse() {
        Map<String, String> validMap = new HashMap<>();
        validMap.put("id", "mosip.abis.identify");
        validMap.put("version", "1.1");
        validMap.put("requestId", "abc-123");
        validMap.put("requesttime", "2025-04-22T15:00:00Z");
        validMap.put("referenceId", "ref-456");
        validMap.put("referenceURL", "http://example.com");
        validMap.put("gallery", "gallery-name");

        boolean result = Listener.isValidIdentifyRequestDto(validMap);
        assertFalse(result); // Verify the result is false
    }

    /**
     * Tests the consumeLogic method with an unsupported message type.
     * Verifies that no interactions occur with the ProxyAbisController.
     */
    @Test
    void testConsumeLogicWithUnsupportedMessageType() throws Exception {
        Message unknownMessage = mock(Message.class);
        listener.consumeLogic(unknownMessage, "mockAddress");
        verifyNoInteractions(proxyAbisController); // Verify no interactions with the controller
    }

    /**
     * Tests the consumeLogic method when an exception occurs.
     * Verifies that the exception is handled and the controller's async method is called.
     */
    @Test
    void testConsumeLogicWhenExceptionOccurs() throws Exception {
        String json = """
            {
                "id": "mosip.abis.insert"
            }
            """;
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(json);

        lenient().doThrow(new RuntimeException("API failure"))
                .when(proxyAbisController).saveInsertRequestThroughListner(any(InsertRequestMO.class), anyInt());

        listener.consumeLogic(message, "mockAddress");
        verify(proxyAbisController).executeAsync(any(), anyInt(), anyInt()); // Verify async execution
    }

    /**
     * Tests the isValidFormat method with a valid LocalDateTime string.
     * Verifies that the method returns true.
     */
    @Test
    void testIsValidFormat_validLocalDateTime() {
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String validDate = "2024-04-22T10:00:00.000Z";
        boolean result = Listener.isValidFormat(format, validDate, Locale.ENGLISH);
        assertTrue(result); // Verify the result is true
    }

    /**
     * Tests the isValidFormat method with an invalid date format.
     * Verifies that the method returns false.
     */
    @Test
    void testIsValidFormat_invalidFormat() {
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String invalidDate = "2024/04/22 10:00:00";
        boolean result = Listener.isValidFormat(format, invalidDate, Locale.ENGLISH);
        assertFalse(result); // Verify the result is false
    }

    /**
     * Tests the isValidInsertRequestDto method with an extra key.
     * Verifies that the method returns true.
     */
    @Test
    void testIsValidInsertRequestDto_extraKey_ReturnsTrue() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        map.put("extraKey", "unexpected");

        boolean result = Listener.isValidInsertRequestDto(map);
        assertTrue(result); // Verify the result is true
    }

    /**
     * Tests the errorRequestThroughListner method.
     * Verifies that the response entity is returned with the correct status code.
     */
    @Test
    void testErrorRequestThroughListner_returnsResponseEntity() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "req-001");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceId", "ref-001");
        map.put("referenceURL", "http://example.com");

        ResponseEntity<Object> response = listener.errorRequestThroughListner(new RuntimeException("fail"), map, 1);
        assertNotNull(response); // Verify the response is not null
        assertEquals(200, response.getStatusCode().value()); // Verify the status code
    }

    /**
     * Tests the send method of Listener.
     * Verifies that the method returns true and performs the expected JMS operations.
     */
    @Test
    void testSendMethod_returnsTrue() throws Exception {
        Listener spyListener = Mockito.spy(listener);

        ActiveMQConnectionFactory dummyFactory = mock(ActiveMQConnectionFactory.class);
        ReflectionTestUtils.setField(spyListener, "activeMQConnectionFactory", dummyFactory);

        Session mockSession = mock(Session.class);
        Queue mockQueue = mock(Queue.class);
        MessageProducer mockProducer = mock(MessageProducer.class);
        TextMessage mockTextMessage = mock(TextMessage.class);

        ReflectionTestUtils.setField(spyListener, "session", mockSession);

        when(mockSession.createQueue(anyString())).thenReturn(mockQueue);
        when(mockSession.createProducer(mockQueue)).thenReturn(mockProducer);
        when(mockSession.createTextMessage(anyString())).thenReturn(mockTextMessage);

        boolean result = spyListener.send("testMessage", "testQueue");

        assertTrue(result); // Verify the result is true
        verify(mockSession).createQueue("testQueue");
        verify(mockSession).createProducer(mockQueue);
        verify(mockSession).createTextMessage("testMessage");
        verify(mockProducer).send(mockTextMessage);
        verify(mockProducer).close();
    }

    /**
     * Tests the initialSetup method when an exception is thrown.
     * Verifies that the correct exception is thrown with the expected error code.
     */
    @Test
    void testInitialSetupThrowsException() throws Exception {
        Listener spyListener = Mockito.spy(new Listener(Mockito.mock(ProxyAbisController.class)));

        Field factoryField = Listener.class.getDeclaredField("activeMQConnectionFactory");
        factoryField.setAccessible(true);
        factoryField.set(spyListener, null);

        AbisException exception = assertThrows(AbisException.class, () ->
                ReflectionTestUtils.invokeMethod(spyListener, "initialSetup")
        );
        assertEquals(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(), exception.getErrorCode());
    }
}