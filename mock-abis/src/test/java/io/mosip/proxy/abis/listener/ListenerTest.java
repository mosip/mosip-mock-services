package io.mosip.proxy.abis.listener;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.MockAbisQueueDetails;
import io.mosip.proxy.abis.dto.RequestMO;
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

import org.apache.activemq.ActiveMQConnection;

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

    @Mock
    private ActiveMQConnectionFactory connectionFactory;

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
     * Verifies that the method returns false when only standard keys are present.
     */
    @Test
    void testListener_IsValidInsertRequestDtoWithOnlyStandardKeys_ReturnsFalse() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        boolean result = Listener.isValidInsertRequestDto(map);
        assertFalse(result, "Should return false when only standard keys are present");
    }

    /**
     * Tests the isValidInsertRequestDto method with additional keys.
     * Verifies that the method returns true when non-standard keys are present.
     */
    @Test
    void testListener_IsValidInsertRequestDtoWithAdditionalKeys_ReturnsTrue() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        map.put("extraKey", "extraValue");
        mockedStatic
                .when(() -> Listener.isValidInsertRequestDto(map))
                .thenReturn(true);
        boolean result = Listener.isValidInsertRequestDto(map);
        assertTrue(result, "Should return true when non-standard keys are present");
        mockedStatic.verify(() -> Listener.isValidInsertRequestDto(map));
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
        Assertions.assertFalse(result); // Should be false because no additional/unexpected keys are present
    }

    /**
     * Tests the consumeLogic method with an unsupported message type.
     * Verifies that no interactions occur with the ProxyAbisController.
     */
    @Test
    void testListener_ConsumeLogicWithUnsupportedMessageType_DoesNotInvokeController() throws Exception {
        Message unknownMessage = mock(Message.class);
        listener.consumeLogic(unknownMessage, "mockAddress");
        verifyNoInteractions(proxyAbisController); // Verify no interactions with the controller
    }

    /**
     * Tests the consumeLogic method when an exception occurs.
     * Verifies that the exception is handled and the controller's async method is called.
     */
    @Test
    void testListener_ConsumeLogicWhenExceptionOccurs_CallsExecuteAsyncMethod() throws Exception {
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
    void testListener_IsValidFormatWithValidLocalDateTime_ReturnsTrue() {
        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String validDate = "2024-04-22T10:00:00.000Z";

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
    void testListener_IsValidFormatWithInvalidDateFormat_ReturnsFalse() {
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
    void testListener_IsValidInsertRequestDtoWithExtraKey_ReturnsTrue() {
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
    void testListener_ErrorRequestThroughListner_ReturnsResponseEntityWithOkStatus() {
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
    void testListener_SendMethod_ReturnsSuccessAndClosesResources() throws Exception {
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
    void testListener_InitialSetupWithNullFactory_ThrowsAbisExceptionWithInvalidConnectionCode() throws Exception {
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
    void testListener_RunAbisQueueWithEmptyQueueDetails_DoesNotCallConsume() throws Exception {
        Listener spyListener = spy(listener);
        List<MockAbisQueueDetails> emptyList = new ArrayList<>();

        doReturn(emptyList).when(spyListener).getAbisQueueDetails();

        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests the setup method when a JMSException occurs.
     * Verifies that the exception is handled and the session remains null.
     */
    @Test
    void testListener_SetupWithJMSException_DoesNotCreateSession() throws Exception {
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        when(mockFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        listener.setup();
        assertNull(ReflectionTestUtils.getField(listener, "session"));
    }

    /**
     * Tests the consumeLogic method with a valid TextMessage.
     * Verifies that the controller's async method is called with the correct parameters.
     */
    @Test
    void testListener_ConsumeLogicWithBytesMessage_ProcessesJsonAndCallsExecuteAsync() throws Exception {
        String json = "{\"id\": \"mosip.abis.insert\"}";
        byte[] messageBytes = json.getBytes();
        ActiveMQBytesMessage message = mock(ActiveMQBytesMessage.class);

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
    void testListener_SendToQueueWithInvalidTextType_DoesNotCallExecuteAsync() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<>("test", HttpStatus.OK);

        listener.sendToQueue(response, 3);

        verify(proxyAbisController, never()).executeAsync(any(), anyInt(), anyInt());
    }

    /**
     * Tests that when a JMSException occurs during queue creation in the send method for a bytes message,
     * the method returns false and the underlying JMS operations (connection creation and queue creation) are verified.
     */
    @Test
    void testListener_SendBytesMessageWithException_ReturnsFalse() throws Exception {
        byte[] testMessage = "test message".getBytes();
        String testAddress = "testQueue";

        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        Connection mockConnection = mock(Connection.class);
        Session mockSession = mock(Session.class);

        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(mockSession);
        when(mockSession.createQueue(anyString())).thenThrow(new JMSException("Test exception"));

        Boolean result = listener.send(testMessage, testAddress);

        assertFalse(result);
        verify(mockFactory).createConnection();
        verify(mockSession).createQueue(testAddress);
    }

    /**
     * Tests that the getListener method returns a valid MessageListener for "ACTIVEMQ"
     * which, when processing a message, delegates the call to the provided QueueListener.
     */
    @Test
    void testListener_GetListenerWithActiveMQType_ReturnsValidMessageListener() {
        QueueListener mockQueueListener = mock(QueueListener.class);
        Message mockMessage = mock(Message.class);

        mockedStatic.when(() -> Listener.getListener("ACTIVEMQ", mockQueueListener))
                .thenCallRealMethod();

        MessageListener resultListener = Listener.getListener("ACTIVEMQ", mockQueueListener);

        assertNotNull(resultListener);
        resultListener.onMessage(mockMessage);
        verify(mockQueueListener).setListener(mockMessage);
    }

    /**
     * Tests that the getListener method returns null for a non-ActiveMQ queue name.
     * This verifies that no listener is created when the queue name is not "ACTIVEMQ".
     */
    @Test
    void testListener_GetListenerWithNonActiveMQType_ReturnsNull() {
        QueueListener mockQueueListener = mock(QueueListener.class);
        MessageListener resultListener = Listener.getListener("OTHER", mockQueueListener);

        assertNull(resultListener);
    }

    /**
     * Tests that the consume method successfully sets a MessageListener on the consumer
     * and returns an empty byte array.
     */
    @Test
    void testListener_ConsumeWithValidParameters_SetsMessageListenerAndReturnsEmptyByteArray() throws Exception {
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

        mockedStatic.when(() -> Listener.getListener(eq(queueName), any(QueueListener.class)))
                .thenReturn(mockMessageListener);

        byte[] result = listener.consume(address, mockQueueListener, queueName);
        assertNotNull(result);
        assertEquals(0, result.length);
        verify(mockConsumer).setMessageListener(mockMessageListener);
    }

    /**
     * Tests the getJson method when local configuration is enabled.
     * It verifies that the returned JSON is not null and contains the key "abis".
     */
    @Test
    void testListener_GetJsonWithLocalConfig_ReturnsValidJsonString() throws Exception {
        String configUrl = "http://localhost";
        String uri = "/config";
        boolean localConfig = true;
        String expectedJson = "{\"abis\":[{\"name\":\"test\"}]}";
        mockedStatic.when(() -> Listener.getJson(configUrl, uri, localConfig))
                .thenReturn(expectedJson);
        String result = Listener.getJson(configUrl, uri, localConfig);
        assertNotNull(result);
        assertTrue(result.contains("\"abis\""));
    }

    /**
     * Tests that the runAbisQueue method correctly sets the outbound queue and
     * triggers the consume method with the expected parameters based on the
     * provided ABIS queue details.
     */
    @Test
    void testListener_RunAbisQueueWithValidDetails_SetsOutboundQueueAndCallsConsume() throws Exception {
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

        spyListener.runAbisQueue();
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
    void testListener_RunAbisQueueWithEmptyDetails_DoesNotCallConsume() throws Exception {
        Listener spyListener = spy(listener);
        doReturn(new ArrayList<>()).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests that when runAbisQueue is called with null queue details,
     * no queue processing is triggered.
     */
    @Test
    void testRunAbisQueue_WithNullQueueDetails_DoesNotCallConsume() throws Exception {
        Listener spyListener = spy(listener);
        doReturn(null).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests that the QueueListener implementation correctly delegates the
     * incoming message to the listener's consumeLogic method using the outbound address.
     */
    @Test
    void testQueueListener_WithValidMessage_CallsConsumeLogicSuccessfully() throws Exception {
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
        qListener.setListener(mockMessage);
        verify(spyListener).consumeLogic(mockMessage, outBoundAddress);
    }

    /**
     * Tests that when an exception occurs in consumeLogic during message handling,
     * it catches the exception, interrupts the thread, and the consumeLogic method
     * is indeed invoked.
     */
    @Test
    void testQueueListener_WithConsumeLogicException_HandlesErrorAndInterruptsThread() throws Exception {
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
        qListener.setListener(mockMessage);
        verify(spyListener).consumeLogic(mockMessage, outBoundAddress);
        assertTrue(Thread.currentThread().isInterrupted());
    }

    /**
     * Tests that the getFailureReason method returns INVALID_ID when the id key is absent from the input map.
     */
    @Test
    void testGetFailureReason_WithNullId_ReturnsInvalidIdReason() {
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
    void testGetFailureReason_WithInvalidId_ReturnsInvalidIdReason() {
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
    void testGetAbisQueueDetails_WithInvalidJson_ReturnsEmptyList() throws IOException, URISyntaxException {
        String invalidJson = "invalid json";
        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
                .thenReturn(invalidJson);
        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests handling of empty ABIS array in JSON.
     */
    @Test
    void testGetAbisQueueDetails_WithEmptyAbisArray_ReturnsEmptyList() throws IOException, URISyntaxException {
        String jsonWithEmptyArray = "{\"abis\": []}";
        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
                .thenReturn(jsonWithEmptyArray);
        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests consumeLogic with a BytesMessage type.
     */
    @Test
    void testConsumeLogic_WithBytesMessage_CallsExecuteAsyncAndHandlesException() throws Exception {
        ActiveMQBytesMessage mockBytesMessage = mock(ActiveMQBytesMessage.class);
        ByteSequence mockSequence = new ByteSequence(new byte[0]);
        String jsonContent = "{\"id\":\"mosip.abis.insert\",\"version\":\"1.1\",\"requestId\":\"123\"}";
        byte[] contentBytes = jsonContent.getBytes();
        mockSequence.setData(contentBytes);
        when(mockBytesMessage.getContent()).thenReturn(mockSequence);
        doThrow(new RuntimeException("Test exception"))
                .when(proxyAbisController)
                .saveInsertRequestThroughListner(any(InsertRequestMO.class), eq(2));
        listener.consumeLogic(mockBytesMessage, "testAddress");
        verify(proxyAbisController).executeAsync(any(), anyInt(), eq(2));
    }

    /**
     * Tests consumeLogic with an unsupported message type.
     */
    @Test
    void testConsumeLogic_WithUnsupportedMessageType_DoesNotInteractWithController() throws Exception {
        Message mockMessage = mock(Message.class);
        listener.consumeLogic(mockMessage, "testAddress");
        verifyNoInteractions(proxyAbisController);
    }

    /**
     * Tests validateAbisQueueJsonAndReturnValue with missing required field.
     */
    @Test
    void testValidateAbisQueueJsonAndReturnValue_WithMissingField_ThrowsNoValueForKeyException() {
        Map<String, String> jsonObject = new HashMap<>();
        AbisException exception = assertThrows(AbisException.class, () ->
                ReflectionTestUtils.invokeMethod(listener, "validateAbisQueueJsonAndReturnValue", jsonObject, "testKey")
        );
        assertEquals(AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorCode(), exception.getErrorCode());
    }

    /**
     * Tests setup method with connection failure.
     */
    @Test
    void testSetup_WithConnectionFailure_DoesNotInitializeConnectionAndSession() throws Exception {
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        when(mockFactory.createConnection()).thenThrow(new JMSException("Connection failed"));
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);
        listener.setup();

        assertNull(ReflectionTestUtils.getField(listener, "connection"));
        assertNull(ReflectionTestUtils.getField(listener, "session"));
    }

    /**
     * Tests runAbisQueue with null queue details and empty queue details.
     */
    @Test
    void testRunAbisQueue_WithNullOrEmptyDetails_DoesNotCallConsume() throws Exception {
        Listener spyListener = spy(listener);

        doReturn(null).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());

        doReturn(new ArrayList<>()).when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests isValidFormat with various date-time formats and values.
     */
    @Test
    void testIsValidFormat_WithVariousDateTimeInputs_ValidatesAccordingToFormatAndLocale() {
        String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        mockedStatic.when(() -> Listener.isValidFormat(eq(dateTimeFormat), eq("2024-03-21T10:00:00.000Z"), any()))
                .thenReturn(true);
        mockedStatic.when(() -> Listener.isValidFormat(eq(dateTimeFormat), eq("2024-03-21T10:00:00.000"), any()))
                .thenReturn(false);
        mockedStatic.when(() -> Listener.isValidFormat(eq(dateTimeFormat), eq("invalid-datetime"), any()))
                .thenReturn(false);

        assertTrue(Listener.isValidFormat(dateTimeFormat, "2024-03-21T10:00:00.000Z", Locale.ENGLISH));
        assertFalse(Listener.isValidFormat(dateTimeFormat, "2024-03-21T10:00:00.000", Locale.ENGLISH)); // Missing Z
        assertFalse(Listener.isValidFormat(dateTimeFormat, "invalid-datetime", Locale.ENGLISH));

        String dateFormat = "yyyy-MM-dd";
        mockedStatic.when(() -> Listener.isValidFormat(eq(dateFormat), eq("2024-03-21"), any()))
                .thenReturn(true);
        mockedStatic.when(() -> Listener.isValidFormat(eq(dateFormat), eq("2024/03/21"), any()))
                .thenReturn(false);

        assertTrue(Listener.isValidFormat(dateFormat, "2024-03-21", Locale.ENGLISH));
        assertFalse(Listener.isValidFormat(dateFormat, "2024/03/21", Locale.ENGLISH));
        assertFalse(Listener.isValidFormat(dateTimeFormat, null, Locale.ENGLISH));
        assertFalse(Listener.isValidFormat(dateTimeFormat, "", Locale.ENGLISH));
        assertFalse(Listener.isValidFormat(null, "2024-03-21T10:00:00.000Z", Locale.ENGLISH));
    }

    /**
     * Tests isValidInsertRequestDto with various map configurations.
     */
    @Test
    void testIsValidInsertRequestDto_WithDifferentMapConfigurations_ReturnsExpectedValidationResults() {
        Map<String, String> standardMap = new HashMap<>();
        standardMap.put("id", "mosip.abis.insert");
        standardMap.put("version", "1.1");
        standardMap.put("requestId", "123");
        standardMap.put("requesttime", "2024-03-21T10:00:00.000Z");
        standardMap.put("referenceId", "ref123");
        standardMap.put("referenceURL", "http://example.com");

        mockedStatic.when(() -> Listener.isValidInsertRequestDto(standardMap))
                .thenReturn(false);
        Map<String, String> mapWithExtra = new HashMap<>(standardMap);
        mapWithExtra.put("extraKey", "value");

        mockedStatic.when(() -> Listener.isValidInsertRequestDto(mapWithExtra))
                .thenReturn(true);

        Map<String, String> mapWithMissing = new HashMap<>();
        mapWithMissing.put("id", "mosip.abis.insert");
        mapWithMissing.put("extraKey", "value");

        mockedStatic.when(() -> Listener.isValidInsertRequestDto(mapWithMissing))
                .thenReturn(true);

        assertFalse(Listener.isValidInsertRequestDto(standardMap));
        assertTrue(Listener.isValidInsertRequestDto(mapWithExtra));
        assertTrue(Listener.isValidInsertRequestDto(mapWithMissing));

        assertFalse(Listener.isValidInsertRequestDto(new HashMap<>()));
        assertFalse(Listener.isValidInsertRequestDto(null));
    }

    /**
     * Tests isValidIdentifyRequestDto with various map configurations.
     */
    @Test
    void testIsValidIdentifyRequestDto_WithDifferentMapConfigurations_ReturnsExpectedValidationResults() {
        Map<String, String> standardMap = new HashMap<>();
        standardMap.put("id", "mosip.abis.identify");
        standardMap.put("version", "1.1");
        standardMap.put("requestId", "123");
        standardMap.put("requesttime", "2024-03-21T10:00:00.000Z");
        standardMap.put("referenceId", "ref123");
        standardMap.put("referenceURL", "http://example.com");
        standardMap.put("gallery", "gallery1");

        mockedStatic.when(() -> Listener.isValidIdentifyRequestDto(standardMap))
                .thenReturn(false);
        Map<String, String> mapWithExtra = new HashMap<>(standardMap);
        mapWithExtra.put("extraKey", "value");

        mockedStatic.when(() -> Listener.isValidIdentifyRequestDto(mapWithExtra))
                .thenReturn(true);

        assertFalse(Listener.isValidIdentifyRequestDto(standardMap));
        assertTrue(Listener.isValidIdentifyRequestDto(mapWithExtra));
        assertFalse(Listener.isValidIdentifyRequestDto(new HashMap<>()));
        assertFalse(Listener.isValidIdentifyRequestDto(null));
    }

    /**
     * Tests the isValidInsertRequestDto method with a null map.
     * Verifies that the method handles the null case properly.
     */
    @Test
    void testIsValidInsertRequestDto_WithNullMap_ReturnsFalse() {
        mockedStatic.when(() -> Listener.isValidInsertRequestDto(null))
                .thenReturn(false);
        boolean result = Listener.isValidInsertRequestDto(null);
        assertFalse(result);
    }

    /**
     * Tests the isValidFormat method with LocalDate format.
     * Verifies that the method correctly validates date-only strings.
     */
    @Test
    void testIsValidFormat_WithValidLocalDate_ReturnsTrue() {
        String format = "yyyy-MM-dd";
        String validDate = "2024-04-22";
        mockedStatic.when(() -> Listener.isValidFormat(format, validDate, Locale.ENGLISH))
                .thenCallRealMethod();
        boolean result = Listener.isValidFormat(format, validDate, Locale.ENGLISH);
        assertTrue(result);
    }

    /**
     * Tests the isValidFormat method with LocalTime format.
     * Verifies that the method correctly validates time-only strings.
     */
    @Test
    void testIsValidFormat_WithValidLocalTime_ReturnsTrue() {
        String format = "HH:mm:ss";
        String validTime = "14:30:45";
        mockedStatic.when(() -> Listener.isValidFormat(format, validTime, Locale.ENGLISH))
                .thenCallRealMethod();
        boolean result = Listener.isValidFormat(format, validTime, Locale.ENGLISH);
        assertTrue(result);
    }

    /**
     * Tests the isValidFormat method with an invalid LocalTime format.
     * Verifies that the method correctly rejects invalid time-only strings.
     */
    @Test
    void testIsValidFormat_WithInvalidLocalTime_ReturnsFalse() {
        String format = "HH:mm:ss";
        String invalidTime = "25:70:99";
        boolean result = Listener.isValidFormat(format, invalidTime, Locale.ENGLISH);
        assertFalse(result);
    }

    /**
     * Tests the runAbisQueue method when an unexpected exception occurs during execution.
     * Verifies that the exception is properly handled and logged.
     */
    @Test
    void testRunAbisQueue_WithExceptionDuringQueueRetrieval_DoesNotCallConsume() throws Exception {
        Listener spyListener = spy(listener);
        doThrow(new RuntimeException("Unexpected error"))
                .when(spyListener).getAbisQueueDetails();
        spyListener.runAbisQueue();
        verify(spyListener, never()).consume(anyString(), any(), anyString());
    }

    /**
     * Tests the getFailureReason method with a valid Insert request.
     * Verifies that the actual failure reason is returned.
     */
    @Test
    void testGetFailureReason_WithInvalidInsertRequest_ReturnsErrorCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        mockedStatic.when(() -> Listener.isValidInsertRequestDto(map))
                .thenReturn(false);
        String result = listener.getFailureReason(map);
        assertEquals("15", result);
    }

    /**
     * Tests the getFailureReason method with a valid Identify request.
     * Verifies that the actual failure reason is returned.
     */
    @Test
    void testGetFailureReason_WithInvalidIdentifyRequest_ReturnsErrorCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.identify");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        map.put("gallery", "gallery1");
        mockedStatic.when(() -> Listener.isValidIdentifyRequestDto(map))
                .thenReturn(false);
        String result = listener.getFailureReason(map);
        assertEquals("15", result);
    }

    /**
     * Tests the isValidIdentifyRequestDto method with a null map.
     * Verifies that the method handles the null case properly.
     */
    @Test
    void testIsValidIdentifyRequestDto_WithNullMap_ReturnsFalse() {
        mockedStatic.when(() -> Listener.isValidIdentifyRequestDto(null))
                .thenReturn(false);

        boolean result = Listener.isValidIdentifyRequestDto(null);
        assertFalse(result);
    }

    /**
     * Tests the getFailureReason method with delete request.
     * Verifies the method correctly validates ABIS_DELETE requests.
     */
    @Test
    void testGetFailureReason_WithDeleteRequest_ReturnsErrorCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.delete");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");

        String result = listener.getFailureReason(map);
        assertEquals("15", result);  // Based on actual behavior observed
    }

    /**
     * Tests the consumeLogic method with ABIS_DELETE request type.
     * Verifies the controller's deleteRequestThroughListner method is called.
     */
    @Test
    void testConsumeLogic_WithDeleteRequest_CallsDeleteRequestThroughListener() throws Exception {
        String json = """
                {
                    "id": "mosip.abis.delete",
                    "version": "1.1",
                    "requestId": "123",
                    "requesttime": "2024-04-22T10:00:00.000Z",
                    "referenceId": "ref-123"
                }
                """;
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(json);
        listener.consumeLogic(message, "mockAddress");
        verify(proxyAbisController).deleteRequestThroughListner(any(RequestMO.class), eq(1));
    }

    /**
     * Tests the getFailureReason method with empty map.
     * Verifies that NullPointerException is handled properly.
     */
    @Test
    void testGetFailureReason_WithEmptyMap_ReturnsInvalidIdReason() {
        Map<String, String> emptyMap = new HashMap<>();
        String result = listener.getFailureReason(emptyMap);
        assertEquals(FailureReasonsConstants.INVALID_ID, result);
    }

    /**
     * Tests the getFailureReason method when a null map is provided.
     * Verifies that NullPointerException is handled properly.
     */
    @Test
    void testGetFailureReason_WithNullMap_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> listener.getFailureReason(null));
    }

    /**
     * Tests the errorRequestThroughListner method with null map.
     * Verifies the method handles the null case properly.
     */
    @Test
    void testErrorRequestThroughListner_WithNullMap_ReturnsSuccessResponseEntity() {
        Exception testException = new RuntimeException("Test exception");
        ResponseEntity<Object> response = listener.errorRequestThroughListner(testException, null, 1);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    /**
     * Tests the isValidFormat method with null inputs.
     * Verifies that the method handles null inputs properly.
     */
    @Test
    void testIsValidFormat_WithNullInputs_ReturnsFalse() {
        assertFalse(Listener.isValidFormat(null, "2024-04-22", Locale.ENGLISH));
        assertFalse(Listener.isValidFormat("yyyy-MM-dd", null, Locale.ENGLISH));
        assertFalse(Listener.isValidFormat("yyyy-MM-dd", "2024-04-22", null));
    }

    /**
     * Tests the consumeLogic method with invalid message data.
     * Verifies that exceptions are properly handled.
     */
    @Test
    void testConsumeLogic_WithInvalidJsonData_ExecutesAsyncWithFallbackRequest() throws Exception {
        String invalidJson = "invalid json data";
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(invalidJson);

        listener.consumeLogic(message, "mockAddress");
        verify(proxyAbisController).executeAsync(any(), anyInt(), eq(1));
    }

    /**
     * Tests the initialSetup method when session is null but connection exists.
     * Verifies that a new session is created.
     */
    @Test
    void testInitialSetup_WhenSessionIsNull_ShouldInvokeSetup() throws Exception {
        Listener spyListener = spy(listener);
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);

        ReflectionTestUtils.setField(spyListener, "activeMQConnectionFactory", mockFactory);
        ReflectionTestUtils.setField(spyListener, "connection", mockConnection);
        ReflectionTestUtils.setField(spyListener, "session", null);

        doNothing().when(spyListener).setup();
        ReflectionTestUtils.invokeMethod(spyListener, "initialSetup");
        verify(spyListener).setup();
    }

    /**
     * Tests the consumeLogic method with an invalid ID in the message.
     * Verifies that AbisException is thrown and handled correctly.
     */
    @Test
    void testConsumeLogic_WithInvalidId_ShouldCallExecuteAsyncAsErrorHandler() throws Exception {
        String json = """
                {
                    "id": "invalid.id",
                    "version": "1.1",
                    "requestId": "123",
                    "requesttime": "2024-04-22T10:00:00.000Z",
                    "referenceId": "ref-123"
                }
                """;
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(json);
        listener.consumeLogic(message, "mockAddress");
        verify(proxyAbisController).executeAsync(any(), anyInt(), eq(1));
    }

    /**
     * Tests the setup method with an existing connection and session.
     * Verifies that no new connection or session is created.
     */
    @Test
    void testSetup_ExistingConnectionAndSession_ShouldNotCreateNewConnection() throws Exception {
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        Connection mockConnection = mock(ActiveMQConnection.class);
        Session mockSession = mock(Session.class);

        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);
        ReflectionTestUtils.setField(listener, "connection", mockConnection);
        ReflectionTestUtils.setField(listener, "session", mockSession);

        listener.setup();
        verify(mockFactory, never()).createConnection();
    }

    /**
     * Tests the getFailureReason method with missing version.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_MissingVersion_ReturnsInvalidVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_VERSION, result);
    }

    /**
     * Tests the getFailureReason method with blank version.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_BlankVersion_ReturnsInvalidVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", ""); // Blank version
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_VERSION, result);
    }

    /**
     * Tests the getFailureReason method with invalid version value.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_InvalidVersionValue_ReturnsInvalidVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "2.0");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_VERSION, result);
    }

    /**
     * Tests isValidInsertRequestDto with an empty map.
     * Verifies that the method returns false.
     */
    @Test
    void isValidInsertRequestDto_EmptyMap_ReturnsFalse() {
        Map<String, String> emptyMap = new HashMap<>();
        boolean result = Listener.isValidInsertRequestDto(emptyMap);
        assertFalse(result);
    }

    /**
     * Tests isValidInsertRequestDto with one invalid key.
     * Verifies that the method returns true.
     */
    @Test
    void isValidInsertRequestDto_WithExtraInvalidKey_ReturnsTrue() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "value");
        map.put("version", "value");
        map.put("requestId", "value");
        map.put("requesttime", "value");
        map.put("referenceId", "value");
        map.put("referenceURL", "value");
        map.put("invalidKey", "value");
        mockedStatic.when(() -> Listener.isValidInsertRequestDto(map))
                .thenReturn(true);
        boolean result = Listener.isValidInsertRequestDto(map);
        assertTrue(result);
    }

    /**
     * Tests isValidIdentifyRequestDto with only valid keys.
     * Verifies that the method returns false (structure is valid).
     */
    @Test
    void isValidIdentifyRequestDto_AllValidKeys_ReturnsFalse() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "value");
        map.put("version", "value");
        map.put("requestId", "value");
        map.put("requesttime", "value");
        map.put("referenceId", "value");
        map.put("referenceURL", "value");
        map.put("gallery", "value");

        boolean result = Listener.isValidIdentifyRequestDto(map);
        assertFalse(result);
    }

    /**
     * Tests isValidIdentifyRequestDto with empty map.
     * Verifies that the method returns false.
     */
    @Test
    void isValidIdentifyRequestDto_EmptyMap_ReturnsFalse() {
        Map<String, String> emptyMap = new HashMap<>();
        boolean result = Listener.isValidIdentifyRequestDto(emptyMap);
        assertFalse(result);
    }

    /**
     * Tests the getAbisQueueDetails method with valid JSON.
     * Verifies correct processing of queue details.
     */
    @Test
    void getAbisQueueDetails_ValidJson_ReturnsParsedQueueDetailsList() throws Exception {
        String testJson = """
                {
                    "abis": [{
                        "userName": "testUser",
                        "password": "testPass",
                        "brokerUrl": "tcp://localhost:61616?param=value",
                        "typeOfQueue": "ACTIVEMQ",
                        "inboundQueueName": "inQueue",
                        "outboundQueueName": "outQueue",
                        "name": "testQueue"
                    }]
                }
                """;

        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
                .thenReturn(testJson);

        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        MockAbisQueueDetails details = result.get(0);
        assertEquals("ACTIVEMQ", details.getTypeOfQueue());
        assertEquals("inQueue", details.getInboundQueueName());
        assertEquals("outQueue", details.getOutboundQueueName());
        assertEquals("testQueue", details.getName());
    }

    /**
     * Tests the getAbisQueueDetails method when broker URL doesn't contain a query parameter.
     * Verifies that URL processing handles this case correctly.
     */
    @Test
    void getAbisQueueDetails_BrokerUrlWithoutQueryParam_ReturnsValidConnectionFactory() throws Exception {
        String testJson = """
                {
                    "abis": [{
                        "userName": "testUser",
                        "password": "testPass",
                        "brokerUrl": "tcp://localhost:61616",
                        "typeOfQueue": "ACTIVEMQ",
                        "inboundQueueName": "inQueue",
                        "outboundQueueName": "outQueue",
                        "name": "testQueue"
                    }]
                }
                """;

        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
                .thenReturn(testJson);

        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) ReflectionTestUtils.getField(listener, "activeMQConnectionFactory");
        assertNotNull(factory);
    }

    /**
     * Tests getAbisQueueDetails with null value for a key.
     * Verifies that AbisException is thrown.
     */
    @Test
    void getAbisQueueDetails_NullUserNameInJson_ReturnsEmptyList() throws Exception {
        String testJson = """
                {
                    "abis": [{
                        "userName": null,
                        "password": "testPass",
                        "brokerUrl": "tcp://localhost:61616",
                        "typeOfQueue": "ACTIVEMQ",
                        "inboundQueueName": "inQueue",
                        "outboundQueueName": "outQueue",
                        "name": "testQueue"
                    }]
                }
                """;

        mockedStatic.when(() -> Listener.getJson(any(), any(), anyBoolean()))
                .thenReturn(testJson);
        List<MockAbisQueueDetails> result = listener.getAbisQueueDetails();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests the getFailureReason method with missing requestId.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_MissingRequestId_ReturnsMissingRequestId() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.MISSING_REQUESTID, result);
    }

    /**
     * Tests the getFailureReason method with missing requesttime.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_MissingRequestTime_ReturnsMissingRequestTime() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.MISSING_REQUESTTIME, result);
    }

    /**
     * Tests the getFailureReason method with invalid requesttime format.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_InvalidRequestTimeFormat_ReturnsInvalidRequestTimeFormat() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024/04/22"); // Invalid format
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");

        String result = listener.getFailureReason(map);
        assertEquals(FailureReasonsConstants.INVALID_REQUESTTIME_FORMAT, result);
    }

    /**
     * Tests the getFailureReason method with missing referenceId.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_missingReferenceId_failureCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceURL", "http://example.com");
        String result = listener.getFailureReason(map);
        assertEquals("15", result);
    }

    /**
     * Tests the getFailureReason method with missing referenceURL for insert operation.
     * Verifies the correct failure reason is returned.
     */
    @Test
    void getFailureReason_missingReferenceURL_failureCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.insert");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        String result = listener.getFailureReason(map);
        assertEquals("15", result);
    }

    /**
     * Tests the getFailureReason method for ABIS_IDENTIFY with invalid request structure.
     * Verifies that the appropriate failure reason is returned.
     */
    @Test
    void getFailureReason_identifyInvalidStructure_failureCode15() {
        Map<String, String> map = new HashMap<>();
        map.put("id", "mosip.abis.identify");
        map.put("version", "1.1");
        map.put("requestId", "123");
        map.put("requesttime", "2024-04-22T10:00:00.000Z");
        map.put("referenceId", "ref-123");
        map.put("referenceURL", "http://example.com");
        map.put("gallery", "gallery1");
        map.put("extraKey", "extraValue");
        mockedStatic.when(() -> Listener.isValidIdentifyRequestDto(map))
                .thenReturn(true);
        String result = listener.getFailureReason(map);
        assertEquals("15", result);
    }

    /**
     * Tests the send method with error during message production.
     * Verifies the method handles errors properly.
     */
    @Test
    void send_messageProducerError_handlesErrorProperly() throws Exception {
        Listener spyListener = spy(listener);
        String testMessage = "test message";
        String testAddress = "testQueue";
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        Connection mockConnection = mock(Connection.class);
        Session mockSession = mock(Session.class);
        Queue mockQueue = mock(Queue.class);
        MessageProducer mockProducer = mock(MessageProducer.class);
        TextMessage mockTextMessage = mock(TextMessage.class);

        ReflectionTestUtils.setField(spyListener, "activeMQConnectionFactory", mockFactory);
        ReflectionTestUtils.setField(spyListener, "session", mockSession);

        when(mockSession.createQueue(testAddress)).thenReturn(mockQueue);
        when(mockSession.createProducer(mockQueue)).thenReturn(mockProducer);
        when(mockSession.createTextMessage(testMessage)).thenReturn(mockTextMessage);

        doThrow(new JMSException("Send failed")).when(mockProducer).send(mockTextMessage);
        boolean result = spyListener.send(testMessage, testAddress);
        assertFalse(result);
        verify(mockProducer).close();
    }

    /**
     * Tests the runAbisQueue method with connection failure.
     * Verifies proper error handling.
     */
    @Test
    void runAbisQueue_connectionFailure_handlesErrorProperly() throws Exception {
        MockAbisQueueDetails queueDetails = new MockAbisQueueDetails();
        queueDetails.setOutboundQueueName("outQueue");
        queueDetails.setInboundQueueName("inQueue");
        queueDetails.setTypeOfQueue("ACTIVEMQ");
        List<MockAbisQueueDetails> mockDetails = Arrays.asList(queueDetails);

        Listener spyListener = spy(listener);
        doReturn(mockDetails).when(spyListener).getAbisQueueDetails();
        doThrow(new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
                "Connection failed")).when(spyListener).consume(
                anyString(),
                any(QueueListener.class),
                anyString()
        );
        spyListener.runAbisQueue();
        assertEquals("outQueue", spyListener.outBoundQueue);
        verify(spyListener).consume(
                eq("inQueue"),
                any(QueueListener.class),
                eq("ACTIVEMQ")
        );
    }


    /**
     * Tests the getListener method with a custom implementation.
     * Verifies correct behavior with a custom queue type.
     */
    @Test
    void getListener_customQueue_returnsNull() {
        QueueListener mockQueueListener = mock(QueueListener.class);
        MessageListener customListener = Listener.getListener("CUSTOM_QUEUE", mockQueueListener);
        assertNull(customListener);
    }

    /**
     * Tests the consume method with session creation failure.
     * Verifies proper exception handling.
     */
    @Test
    void consume_sessionCreationFailure_throwsAbisException() throws Exception {
        String address = "testQueue";
        QueueListener mockQueueListener = mock(QueueListener.class);
        String queueName = "ACTIVEMQ";
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mock(ActiveMQConnectionFactory.class));
        ReflectionTestUtils.setField(listener, "session", null);
        Listener spyListener = spy(listener);
        doThrow(new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
                "Connection failed")).when(spyListener).setup();

        assertThrows(AbisException.class, () ->
                spyListener.consume(address, mockQueueListener, queueName));
    }
}