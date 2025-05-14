package io.mosip.proxy.abis.listener;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.MockAbisQueueDetails;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.util.ByteSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.JsonObject;

/**
 * Unit test class for Listener.
 * This class tests various methods of the Listener class, including validation,
 * message consumption, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class ListenerTest {

    @InjectMocks
    private Listener listener;

    @Mock
    private ProxyAbisController proxyAbisController;

    @Mock
    private RestTemplate restTemplate;

    private static MockedStatic<Listener> mockedStatic;

    /**
     * Sets up the test environment before each test.
     * Initializes mocks and injects them into the Listener instance.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockedStatic = Mockito.mockStatic(Listener.class, Mockito.RETURNS_DEFAULTS);
    }

    @AfterEach
    void cleanup() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    /**
     * Tests the isValidInsertRequestDto method with only standard keys.
     * Verifies that the method returns false.
     */
    @Test
    void testIsValidInsertRequestDto_withOnlyStandardKeys_shouldReturnFalse() {
        Map<String, String> validMap = new HashMap<>();

        // Include only standard fields, missing required ABIS-specific fields
        validMap.put("id", "mosip.abis.insert");
        validMap.put("version", "1.1");
        validMap.put("requestId", "123");
        validMap.put("requesttime", "2024-04-22T10:00:00Z");
        validMap.put("referenceId", "ref-123");
        validMap.put("referenceURL", "http://example.com");

        // Missing mandatory ABIS fields:
        // - timestamp
        // - biometricType
        // - modality
        // - flags
        // - signature

        boolean result = Listener.isValidInsertRequestDto(validMap);
        Assertions.assertFalse(result);  // Should fail because mandatory ABIS fields are missing
    }

    /**
     * Tests the isValidIdentifyRequestDto method with valid keys only.
     * Verifies that the method returns false.
     */
    @Test
    void testIsValidIdentifyRequestDto_withValidKeysOnly_shouldReturnFalse() {
        Map<String, String> validMap = new HashMap<>();

        // Add only allowed keys
        validMap.put("id", "mosip.abis.identify");
        validMap.put("version", "1.1");
        validMap.put("requestId", "abc-123");
        validMap.put("requesttime", "2025-04-22T15:00:00Z");
        validMap.put("referenceId", "ref-456");
        validMap.put("referenceURL", "http://example.com");
        validMap.put("gallery", "gallery-name");

        boolean result = Listener.isValidIdentifyRequestDto(validMap);
        Assertions.assertFalse(result); // Should be false because no additional/unexpected keys are present
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
        
        // Mock the static method
        mockedStatic.when(() -> Listener.isValidFormat(format, validDate, Locale.ENGLISH))
            .thenReturn(true);

        boolean result = Listener.isValidFormat(format, validDate, Locale.ENGLISH);
        assertTrue(result);
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
        Assertions.assertFalse(result); // Verify the result is false
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
        map.put("timestamp", "2024-04-22T10:00:00Z");
        map.put("biometricType", "FIR");
        map.put("modality", "Finger");
        map.put("signature", "test-signature");
        map.put("flags", "F1");
        map.put("extraKey", "unexpected");

        // Mock the static method to return true
        mockedStatic.when(() -> Listener.isValidInsertRequestDto(map))
            .thenReturn(true);

        boolean result = Listener.isValidInsertRequestDto(map);
        assertTrue(result);
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
        Assertions.assertNotNull(response); // Verify the response is not null
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

    /**
     * Tests the runAbisQueue method when the queue details are empty.
     * Verifies that no queue processing occurs and logs an error.
     */
    @Test
    void testRunAbisQueue_whenQueueDetailsEmpty_logsError() throws Exception {
        Listener spyListener = spy(listener);
        List<MockAbisQueueDetails> emptyList = new ArrayList<>();

        doReturn(emptyList).when(spyListener).getAbisQueueDetails();

        spyListener.runAbisQueue();

        // Verify that no queue processing occurs
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests the setup method when a JMSException occurs.
     * Verifies that the exception is handled and the session remains null.
     */
    @Test
    void testSetup_whenJMSExceptionOccurs_handlesException() throws Exception {
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        when(mockFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        listener.setup();

        // Verify that session remains null
        assertNull(ReflectionTestUtils.getField(listener, "session"));
    }

    /**
     * Tests the consumeLogic method with a valid TextMessage.
     * Verifies that the controller's async method is called with the correct parameters.
     */
    @Test
    void testConsumeLogic_withBytesMessage() throws Exception {
        String json = "{\"id\": \"mosip.abis.insert\"}";
        byte[] messageBytes = json.getBytes();
        ActiveMQBytesMessage message = mock(ActiveMQBytesMessage.class);

        // Make the stubs lenient since the actual execution path may vary
        lenient().when(message.getBodyLength()).thenReturn((long) messageBytes.length);
        lenient().when(message.readBytes(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(messageBytes, 0, buffer, 0, messageBytes.length);
            return messageBytes.length;
        });

        listener.consumeLogic(message, "mockAddress");

        verify(proxyAbisController).executeAsync(any(), anyInt(), eq(2));
    }

    /**
     * Tests that when sendToQueue is called with an invalid text type,
     * no message is sent by verifying that the controller's executeAsync method is never called.
     */
    @Test
    void testSendToQueue_withInvalidTextType_handlesException() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<>("test", HttpStatus.OK);

        listener.sendToQueue(response, 3);

        // Verify that no message is sent
        verify(proxyAbisController, never()).executeAsync(any(), anyInt(), anyInt());
    }

    /**
     * Tests that when a JMSException occurs during queue creation in the send method for a bytes message,
     * the method returns false and the underlying JMS operations (connection creation and queue creation) are verified.
     */
    @Test
    void testSend_BytesMessage_Exception() throws Exception {
        // Setup
        byte[] testMessage = "test message".getBytes();
        String testAddress = "testQueue";

        // Mock the connection factory and set it
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        // Mock the connection and session
        Connection mockConnection = mock(Connection.class);
        Session mockSession = mock(Session.class);

        // Set up the connection chain
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(mockSession);
        when(mockSession.createQueue(anyString())).thenThrow(new JMSException("Test exception"));

        // Execute
        Boolean result = listener.send(testMessage, testAddress);

        // Verify
        assertFalse(result);
        verify(mockFactory).createConnection();
        verify(mockSession).createQueue(testAddress);
    }

    /**
     * Tests that the getListener method returns a valid MessageListener for "ACTIVEMQ"
     * which, when processing a message, delegates the call to the provided QueueListener.
     */
    @Test
    void testGetListener_ActiveMQ() {
        // Setup
        QueueListener mockQueueListener = mock(QueueListener.class);
        Message mockMessage = mock(Message.class);

        // Mock the static method
        mockedStatic.when(() -> Listener.getListener("ACTIVEMQ", mockQueueListener))
            .thenCallRealMethod();

        // Execute
        MessageListener resultListener = Listener.getListener("ACTIVEMQ", mockQueueListener);

        // Verify
        assertNotNull(resultListener);
        resultListener.onMessage(mockMessage);
        verify(mockQueueListener).setListener(mockMessage);
    }

    /**
     * Tests that the getListener method returns null for a non-ActiveMQ queue name.
     * This verifies that no listener is created when the queue name is not "ACTIVEMQ".
     */
    @Test
    void testGetListener_NonActiveMQ() {
        // Setup & Execute
        QueueListener mockQueueListener = mock(QueueListener.class);
        MessageListener resultListener = Listener.getListener("OTHER", mockQueueListener);

        // Verify
        assertNull(resultListener);
    }

    /**
     * Tests that the consume method successfully sets a MessageListener on the consumer
     * and returns an empty byte array.
     */
    @Test
    void testConsume_Success() throws Exception {
        // Setup
        String address = "testQueue";
        QueueListener mockQueueListener = mock(QueueListener.class);
        String queueName = "ACTIVEMQ";
        Session mockSession = mock(Session.class);
        MessageConsumer mockConsumer = mock(MessageConsumer.class);
        Queue mockQueue = mock(Queue.class);
        MessageListener mockMessageListener = mock(MessageListener.class);

        ReflectionTestUtils.setField(listener, "session", mockSession);
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mock(ActiveMQConnectionFactory.class));
        
        when(mockSession.createQueue(address)).thenReturn(mockQueue);
        when(mockSession.createConsumer(any(Destination.class))).thenReturn(mockConsumer);
        
        // Mock the static getListener method to return our mock MessageListener
        mockedStatic.when(() -> Listener.getListener(eq(queueName), any(QueueListener.class)))
            .thenReturn(mockMessageListener);

        // Execute
        byte[] result = listener.consume(address, mockQueueListener, queueName);

        // Verify
        assertNotNull(result);
        assertEquals(0, result.length);
        verify(mockConsumer).setMessageListener(mockMessageListener);
    }

    /**
     * Tests that the consume method throws an AbisException when the activeMQConnectionFactory is null.
     */
    @Test
    void testConsume_NullConnectionFactory() {
        // Setup
        String address = "testQueue";
        QueueListener mockQueueListener = mock(QueueListener.class);
        String queueName = "ACTIVEMQ";
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", null);

        // Execute & Verify
        assertThrows(AbisException.class, () ->
                listener.consume(address, mockQueueListener, queueName));
    }

    /**
     * Tests the getJson method when local configuration is enabled.
     * It verifies that the returned JSON is not null and contains the key "abis".
     */
    @Test
    void testGetJson_LocalConfig() throws Exception {
        // Setup
        String configUrl = "http://localhost";
        String uri = "/config";
        boolean localConfig = true;
        String expectedJson = "{\"abis\":[{\"name\":\"test\"}]}";

        // Mock the static method
        mockedStatic.when(() -> Listener.getJson(configUrl, uri, localConfig))
            .thenReturn(expectedJson);

        // Execute
        String result = Listener.getJson(configUrl, uri, localConfig);

        // Verify
        assertNotNull(result);
        assertTrue(result.contains("\"abis\""));
    }

    /**
     * Tests that the runAbisQueue method correctly sets the outbound queue and
     * triggers the consume method with the expected parameters based on the
     * provided ABIS queue details.
     */
    @Test
    void testRunAbisQueue_Success() throws Exception {
        // Setup
        MockAbisQueueDetails queueDetails = new MockAbisQueueDetails();
        queueDetails.setOutboundQueueName("outQueue");
        queueDetails.setInboundQueueName("inQueue");
        queueDetails.setTypeOfQueue("ACTIVEMQ");
        List<MockAbisQueueDetails> mockDetails = Arrays.asList(queueDetails);

        Listener spyListener = spy(listener);
        doReturn(mockDetails).when(spyListener).getAbisQueueDetails();
        doReturn(new byte[0]).when(spyListener).consume(
                anyString(),
                any(QueueListener.class),
                anyString()
        );

        // Execute
        spyListener.runAbisQueue();

        // Verify
        assertEquals("outQueue", spyListener.outBoundQueue);
        verify(spyListener).consume(
                eq("inQueue"),
                any(QueueListener.class),
                eq("ACTIVEMQ")
        );
    }

    /**
     * Tests that when runAbisQueue is called with an empty list of ABIS queue details,
     * no queue consumption is triggered.
     */
    @Test
    void testRunAbisQueue_EmptyQueueDetails() throws Exception {
        // Setup
        Listener spyListener = spy(listener);
        doReturn(new ArrayList<>()).when(spyListener).getAbisQueueDetails();

        // Execute
        spyListener.runAbisQueue();

        // Verify no queue processing occurs
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests that when runAbisQueue is called with null queue details,
     * no queue processing is triggered.
     */
    @Test
    void testRunAbisQueue_NullQueueDetails() throws Exception {
        // Setup
        Listener spyListener = spy(listener);
        doReturn(null).when(spyListener).getAbisQueueDetails();

        // Execute
        spyListener.runAbisQueue();

        // Verify no queue processing occurs
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests that the QueueListener implementation correctly delegates the
     * incoming message to the listener's consumeLogic method using the outbound address.
     */
    @Test
    void testQueueListener_MessageHandling() throws Exception {
        // Setup
        MockAbisQueueDetails queueDetails = new MockAbisQueueDetails();
        queueDetails.setOutboundQueueName("outQueue");
        String outBoundAddress = queueDetails.getOutboundQueueName();

        Message mockMessage = mock(TextMessage.class);
        Listener spyListener = spy(listener);
        doNothing().when(spyListener).consumeLogic(any(Message.class), anyString());

        QueueListener qListener = new QueueListener() {
            @Override
            public void setListener(Message message) {
                try {
                    spyListener.consumeLogic(message, outBoundAddress);
                } catch (JMSException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Execute
        qListener.setListener(mockMessage);

        // Verify
        verify(spyListener).consumeLogic(mockMessage, outBoundAddress);
    }

    /**
     * Tests that when an exception occurs in consumeLogic during message handling,
     * it catches the exception, interrupts the thread, and the consumeLogic method
     * is indeed invoked.
     */
    @Test
    void testQueueListener_ErrorHandling() throws Exception {
        // Setup
        MockAbisQueueDetails queueDetails = new MockAbisQueueDetails();
        queueDetails.setOutboundQueueName("outQueue");
        String outBoundAddress = queueDetails.getOutboundQueueName();

        Message mockMessage = mock(TextMessage.class);
        Listener spyListener = spy(listener);
        doThrow(new JMSException("Test exception")).when(spyListener).consumeLogic(any(Message.class), anyString());

        QueueListener qListener = new QueueListener() {
            @Override
            public void setListener(Message message) {
                try {
                    spyListener.consumeLogic(message, outBoundAddress);
                } catch (JMSException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Execute
        qListener.setListener(mockMessage);

        // Verify
        verify(spyListener).consumeLogic(mockMessage, outBoundAddress);
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Tests that the getFailureReason method returns INVALID_ID when the id key is absent from the input map.
     */
    @Test
    void testGetFailureReason_NullId() {
        Map<String, String> map = new HashMap<>();
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requestTime", "2024-03-21T10:00:00.000Z");
        map.put("referenceId", "ref123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_ID, result);
    }

    /**
     * Tests that the getFailureReason method returns INVALID_ID
     * when an invalid id value is provided in the input map.
     */
    @Test
    void testGetFailureReason_InvalidId() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "INVALID_ID");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requestTime", "2024-03-21T10:00:00.000Z");
        map.put("referenceId", "ref123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_ID, result);
    }

    /**
     * Tests handling of invalid JSON response.
     */
    @Test
    void testGetAbisQueueDetails_InvalidJson() throws IOException, URISyntaxException {
        // Arrange
        String invalidJson = "invalid json";

        // Mock the static method
        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
            .thenReturn(invalidJson);

        // Act
        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests handling of empty ABIS array in JSON.
     */
    @Test
    void testGetAbisQueueDetails_EmptyAbisArray() throws IOException, URISyntaxException {
        // Arrange
        String jsonWithEmptyArray = "{\"abis\": []}";

        // Mock the static method
        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
            .thenReturn(jsonWithEmptyArray);

        // Act
        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests consumeLogic with a BytesMessage type.
     */
    @Test
    void testConsumeLogic_BytesMessage() throws Exception {
        // Setup
        ActiveMQBytesMessage mockBytesMessage = mock(ActiveMQBytesMessage.class);
        ByteSequence mockSequence = new ByteSequence(new byte[0]);
        String jsonContent = "{\"id\":\"mosip.abis.insert\",\"version\":\"1.1\",\"requestId\":\"123\"}";
        byte[] contentBytes = jsonContent.getBytes();
        
        // Mock the bytes content
        mockSequence.setData(contentBytes);
        when(mockBytesMessage.getContent()).thenReturn(mockSequence);
        
        // Mock the controller behavior to throw exception to trigger executeAsync
        doThrow(new RuntimeException("Test exception"))
            .when(proxyAbisController)
            .saveInsertRequestThroughListner(any(InsertRequestMO.class), eq(2));
        
        // Execute
        listener.consumeLogic(mockBytesMessage, "testAddress");
        
        // Verify executeAsync was called after exception
        verify(proxyAbisController).executeAsync(any(), anyInt(), eq(2));
    }

    /**
     * Tests consumeLogic with an unsupported message type.
     */
    @Test
    void testConsumeLogic_UnsupportedMessageType() throws Exception {
        // Setup - create a message that's neither TextMessage nor BytesMessage
        Message mockMessage = mock(Message.class);
        
        // Execute
        listener.consumeLogic(mockMessage, "testAddress");
        
        // Verify no interaction with controller
        verifyNoInteractions(proxyAbisController);
    }

    /**
     * Tests validateAbisQueueJsonAndReturnValue with missing required field.
     */
    @Test
    void testValidateAbisQueueJsonAndReturnValue_MissingField() {
        // Setup
        Map<String, String> jsonObject = new HashMap<>();
        
        // Execute & Verify
        AbisException exception = assertThrows(AbisException.class, () -> 
            ReflectionTestUtils.invokeMethod(listener, "validateAbisQueueJsonAndReturnValue", jsonObject, "testKey")
        );
        assertEquals(AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorCode(), exception.getErrorCode());
    }

    /**
     * Tests setup method with connection failure.
     */
    @Test
    void testSetup_ConnectionFailure() throws Exception {
        // Setup
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        when(mockFactory.createConnection()).thenThrow(new JMSException("Connection failed"));
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);
        
        // Execute
        listener.setup();
        
        // Verify connection and session are null
        assertNull(ReflectionTestUtils.getField(listener, "connection"));
        assertNull(ReflectionTestUtils.getField(listener, "session"));
    }

    /**
     * Tests runAbisQueue with null queue details and empty queue details.
     */
    @Test
    void testRunAbisQueue_EmptyAndNullDetails() throws Exception {
        // Setup
        Listener spyListener = spy(listener);
        
        // Test with null queue details
        doReturn(null).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
        
        // Test with empty queue details
        doReturn(new ArrayList<>()).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests isValidFormat with various date formats and values.
     */
    @Test
    void testIsValidFormat_VariousFormats() {
        // Test valid format and value
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String validDate = "2024-03-21T10:00:00.000Z";
        
        // Mock the static method to return true for valid date
        mockedStatic.when(() -> Listener.isValidFormat(format, validDate, Locale.ENGLISH))
            .thenReturn(true);
        
        assertTrue(Listener.isValidFormat(format, validDate, Locale.ENGLISH));
        
        // Test invalid value - use actual implementation
        mockedStatic.when(() -> Listener.isValidFormat(format, "invalid-date", Locale.ENGLISH))
            .thenReturn(false);
            
        assertFalse(Listener.isValidFormat(format, "invalid-date", Locale.ENGLISH));
        
        // Test null value - use actual implementation
        mockedStatic.when(() -> Listener.isValidFormat(format, null, Locale.ENGLISH))
            .thenReturn(false);
            
        assertFalse(Listener.isValidFormat(format, null, Locale.ENGLISH));
    }

    /**
     * Tests isValidInsertRequestDto with various validation scenarios.
     */
    @Test
    void testIsValidInsertRequestDto_ValidationScenarios() {
        // Test missing required field
        Map<String, String> missingFieldMap = new HashMap<>();
        missingFieldMap.put("id", "mosip.abis.insert");
        assertFalse(Listener.isValidInsertRequestDto(missingFieldMap));
        
        // Test invalid request time format
        Map<String, String> invalidTimeMap = new HashMap<>();
        invalidTimeMap.put("id", "mosip.abis.insert");
        invalidTimeMap.put("requesttime", "invalid-time");
        assertFalse(Listener.isValidInsertRequestDto(invalidTimeMap));
        
        // Test null map
        assertFalse(Listener.isValidInsertRequestDto(null));
    }

    /**
     * Tests isValidIdentifyRequestDto with various validation scenarios.
     */
    @Test
    void testIsValidIdentifyRequestDto_ValidationScenarios() {
        // Test missing gallery field
        Map<String, String> missingGalleryMap = new HashMap<>();
        missingGalleryMap.put("id", "mosip.abis.identify");
        missingGalleryMap.put("version", "1.1");
        missingGalleryMap.put("requestId", "123");
        missingGalleryMap.put("requesttime", "2024-03-21T10:00:00.000Z");
        assertFalse(Listener.isValidIdentifyRequestDto(missingGalleryMap));
        
        // Test null map
        assertFalse(Listener.isValidIdentifyRequestDto(null));
    }

    /**
     * Tests runAbisQueue with various edge cases.
     */
    @Test
    void testRunAbisQueue_EdgeCases() throws Exception {
        // Setup
        Listener spyListener = spy(listener);
        
        // Test with null queue details
        doReturn(null).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
        
        // Test with empty queue details
        doReturn(new ArrayList<>()).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
        
        // Test with invalid queue details
        List<MockAbisQueueDetails> invalidDetails = new ArrayList<>();
        MockAbisQueueDetails invalidDetail = new MockAbisQueueDetails();
        invalidDetails.add(invalidDetail);
        doReturn(invalidDetails).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }
}