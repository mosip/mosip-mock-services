// File: src/test/java/io/mosip/proxy/abis/listener/ListenerTest.java
package io.mosip.proxy.abis.listener;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.MessageProducer;
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListenerTest {

    @InjectMocks
    private Listener listener; // Instance of Listener with mocks injected

    @Mock
    private ProxyAbisController proxyAbisController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

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
        assertFalse(result);
    }

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
        assertFalse(result);
    }

    @Test
    void testConsumeLogicWithUnsupportedMessageType() throws Exception {
        Message unknownMessage = mock(Message.class);
        listener.consumeLogic(unknownMessage, "mockAddress");
        verifyNoInteractions(proxyAbisController);
    }

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
        verify(proxyAbisController).executeAsync(any(), anyInt(), anyInt());
    }

    @Test
    void testIsValidFormat_validLocalDateTime() {
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String validDate = "2024-04-22T10:00:00.000Z";
        boolean result = Listener.isValidFormat(format, validDate, Locale.ENGLISH);
        assertTrue(result);
    }

    @Test
    void testIsValidFormat_invalidFormat() {
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String invalidDate = "2024/04/22 10:00:00";
        boolean result = Listener.isValidFormat(format, invalidDate, Locale.ENGLISH);
        assertFalse(result);
    }

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
        assertTrue(result);
    }

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
        // Using getStatusCode().value() instead of the deprecated getStatusCodeValue()
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testConsumeLogic_handlesExceptionAndCallsAsync() throws JMSException, InterruptedException {
        String json = """
            {
                "id": "mosip.abis.insert",
                "version": "1.1",
                "requestId": "123",
                "requesttime": "2024-04-22T10:00:00Z",
                "referenceId": "ref-123",
                "referenceURL": "http://example.com",
                "extraKey": "unexpected"
            }
            """;
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(json);

        lenient().doThrow(new RuntimeException("insert failure"))
                .when(proxyAbisController).saveInsertRequestThroughListner(any(InsertRequestMO.class), anyInt());

        listener.consumeLogic(message, "dummyAddress");
        verify(proxyAbisController).executeAsync(any(), anyInt(), anyInt());
    }

    @Test
    void testGetFailureReason_invalidId() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "invalid.id");
        map.put("version", "1.1");
        map.put("requestId", "req");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceId", "ref");
        map.put("referenceURL", "http://example.com");
        String reason = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_ID, reason);
    }

    @Test
    void testGetFailureReason_invalidVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "2.0");
        map.put("requestId", "req");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceId", "ref");
        map.put("referenceURL", "http://example.com");
        String reason = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_VERSION, reason);
    }

    @Test
    void testGetFailureReason_missingRequestId() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceId", "ref");
        map.put("referenceURL", "http://example.com");
        String reason = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.MISSING_REQUESTID, reason);
    }

    @Test
    void testGetFailureReason_missingReferenceId() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "req");
        map.put("requesttime", "2024-04-22T10:00:00Z");
        map.put("referenceURL", "http://example.com");
        String reason = listener.getFailureReason(map);
        assertEquals("1" + FailureReasonsConstants.MISSING_REFERENCEID, reason);
    }

    @Test
    void testGetFailureReason_invalidTimeFormat() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "req");
        map.put("requesttime", "invalid-time");
        map.put("referenceId", "ref");
        map.put("referenceURL", "http://example.com");
        String reason = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_REQUESTTIME_FORMAT, reason);
    }

    // File: src/test/java/io/mosip/proxy/abis/listener/ListenerTest.java
    @Test
    void testSendMethod_returnsTrue() throws Exception {
        // Create a spy for the Listener instance first
        Listener spyListener = Mockito.spy(listener);

        // Inject a dummy ActiveMQConnectionFactory so that initialSetup() passes
        ActiveMQConnectionFactory dummyFactory = mock(ActiveMQConnectionFactory.class);
        ReflectionTestUtils.setField(spyListener, "activeMQConnectionFactory", dummyFactory);

        // Create mocks for Session, Queue, MessageProducer, and TextMessage
        Session mockSession = mock(Session.class);
        Queue mockQueue = mock(Queue.class);
        MessageProducer mockProducer = mock(MessageProducer.class);
        TextMessage mockTextMessage = mock(TextMessage.class);

        // Inject the mock Session into the spyListener instance
        ReflectionTestUtils.setField(spyListener, "session", mockSession);

        // Stub behaviors for session methods
        when(mockSession.createQueue(anyString())).thenReturn(mockQueue);
        when(mockSession.createProducer(mockQueue)).thenReturn(mockProducer);
        when(mockSession.createTextMessage(anyString())).thenReturn(mockTextMessage);

        // Call the send() method; initialSetup() will now work since dummyFactory is injected
        boolean result = spyListener.send("testMessage", "testQueue");

        // Verify that send() returns true and calls the expected JMS operations
        assertTrue(result);
        verify(mockSession).createQueue("testQueue");
        verify(mockSession).createProducer(mockQueue);
        verify(mockSession).createTextMessage("testMessage");
        verify(mockProducer).send(mockTextMessage);
        verify(mockProducer).close();
    }

    @Test
    void testInitialSetupThrowsException() throws Exception {
        // Create a spy of the Listener
        Listener spyListener = Mockito.spy(new Listener(Mockito.mock(ProxyAbisController.class)));

        Field factoryField = Listener.class.getDeclaredField("activeMQConnectionFactory");
        factoryField.setAccessible(true);
        factoryField.set(spyListener, null);

        // Using ReflectionTestUtils.invokeMethod to access the private initialSetup() method
        AbisException exception = assertThrows(AbisException.class, () ->
                ReflectionTestUtils.invokeMethod(spyListener, "initialSetup")
        );
        assertEquals(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(), exception.getErrorCode());
    }
}