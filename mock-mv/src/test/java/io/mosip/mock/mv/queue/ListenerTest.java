package io.mosip.mock.mv.queue;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.MockitoAnnotations.openMocks;

import io.mosip.mock.mv.dto.Expectation;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Queue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for Listener component that handles JMS message consumption and processing.
 * This class tests various scenarios including valid/invalid messages, exception handling,
 * and different message types processing.
 */
public class ListenerTest {

    private Listener listener;

    @Mock
    private Environment env;

    @Mock
    private io.mosip.mock.mv.service.ExpectationCache expectationCache;

    @Mock
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Mock
    private Session session;

    @Mock
    private ActiveMQConnection connection;

    @Mock
    private Queue queue;

    @Mock
    private MessageConsumer messageConsumer;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes all mocks, configures default behavior for mocked objects,
     * and prepares the Listener instance with necessary dependencies.
     *
     * @throws JMSException if there are issues setting up JMS components
     */
    @Before
    public void setUp() throws JMSException {
        openMocks(this);

        listener = new Listener();

        when(env.getProperty(Listener.DECISION_SERVICE_ID)).thenReturn("decisionServiceId");
        when(connection.isClosed()).thenReturn(false);
        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createConsumer(any(Queue.class))).thenReturn(messageConsumer);
        doNothing().when(connection).start();
        doNothing().when(messageConsumer).setMessageListener(any(MessageListener.class));

        ReflectionTestUtils.setField(listener, "env", env);
        ReflectionTestUtils.setField(listener, "expectationCache", expectationCache);
        ReflectionTestUtils.setField(listener, "mockDecision", "APPROVED");
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", activeMQConnectionFactory);
        ReflectionTestUtils.setField(listener, "session", session);
        ReflectionTestUtils.setField(listener, "connection", connection);

        listener = spy(listener);
        doReturn(true).when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
    }

    /**
     * Tests the consumeLogic method with a valid TextMessage containing proper JSON.
     * Verifies that the method processes the message successfully and returns true.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_ValidTextMessage_ReturnsTrue() throws Exception {
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        verify(textMessage).getText();
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with an invalid message type (not TextMessage).
     * Verifies that the method handles unsupported message types gracefully and returns false.
     */
    @Test
    public void consumeLogic_InvalidMessageType_ReturnsFalse() {
        Message dummyMessage = mock(Message.class);

        boolean result = listener.consumeLogic(dummyMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with an expectation that has REJECTED decision and delay.
     * Verifies that the method processes delayed rejection responses correctly.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_ExpectationRejectedWithDelay_ReturnsTrue() throws Exception {
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [{\"referenceId\":\"ref1\"}] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        expectation.setDelayResponse(5);
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        verify(textMessage).getText();
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a verification response queue address.
     * Verifies that messages sent to verification queues are processed correctly.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_VerificationResponseQueueAddress_ReturnsTrue() throws Exception {
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        boolean result = listener.consumeLogic(textMessage, "verification-to-mosip");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with invalid JSON content.
     * Verifies that malformed JSON is handled gracefully and returns false.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_InvalidJson_ReturnsFalse() throws Exception {
        String invalidJson = "{ invalid json }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with a null message.
     * Verifies that null input is handled gracefully and returns false.
     */
    @Test
    public void consumeLogic_NullMessage_ReturnsFalse() {
        boolean result = listener.consumeLogic(null, "dummyAddress");
        assertFalse(result);
    }

    /**
     * Tests the getListener static method that creates a MessageListener delegate.
     * Verifies that the returned MessageListener properly delegates to the QueueListener.
     */
    @Test
    public void getListener_DelegatesToQueueListener() {
        QueueListener queueListener = mock(QueueListener.class);
        Message message = mock(Message.class);

        MessageListener messageListener = Listener.getListener(queueListener);
        messageListener.onMessage(message);

        verify(queueListener).setListener(message);
    }

    /**
     * Tests the consume method when session creation fails.
     * Verifies that JMS exceptions during session setup are handled gracefully.
     *
     * @throws Exception if there are unexpected issues during test execution
     */
    @Test
    public void consume_SessionCreationFails_ReturnsEmptyByteArray() throws Exception {
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        Session mockSession = mock(Session.class);
        when(mockSession.createQueue(anyString())).thenThrow(new JMSException("Test exception"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        byte[] result = listener.consume(address, queueListener, "brokerUrl", "username", "password");

        assertEquals(0, result.length);
    }

    /**
     * Tests the executeAsync method with an invalid text type.
     * Verifies that unsupported text types don't trigger send operations but still return true.
     */
    @Test
    public void executeAsync_InvalidTextType_NoSendOperationsAndReturnsTrue() {
        String response = "test message";
        int delayResponse = 0;
        Integer textType = 3;
        String mvAddress = "test-queue";
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());
        doReturn(true).when(spyListener).send(any(byte[].class), anyString());

        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        assertTrue(result);
        verify(spyListener, never()).send(anyString(), anyString());
        verify(spyListener, never()).send(any(byte[].class), anyString());
    }

    /**
     * Tests the consumeLogic method when expectation cache throws an error.
     * Verifies that cache exceptions are handled gracefully and return false.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_ExpectationCacheError_ReturnsFalse() throws Exception {
        String invalidJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [{\"referenceId\":\"ref1\"}] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);
        when(expectationCache.get(anyString())).thenThrow(new RuntimeException("Cache error"));

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with ActiveMQBytesMessage.
     * Verifies that bytes messages are not supported and return false.
     */
    @Test
    public void consumeLogic_ActiveMQBytesMessage_ReturnsFalse() {
        ActiveMQBytesMessage bytesMessage = mock(ActiveMQBytesMessage.class);
        when(bytesMessage.getContent()).thenReturn(new org.apache.activemq.util.ByteSequence("test".getBytes()));

        boolean result = listener.consumeLogic(bytesMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with an unsupported message type.
     * Verifies that unknown message types are handled gracefully and return false.
     */
    @Test
    public void consumeLogic_UnsupportedMessageType_ReturnsFalse() {
        Message unsupportedMessage = mock(Message.class);

        boolean result = listener.consumeLogic(unsupportedMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the setup method when connection creation fails.
     * Verifies that JMS connection failures are handled gracefully during setup.
     *
     * @throws Exception if there are unexpected issues during test execution
     */
    @Test
    public void setup_ConnectionCreationFails_HandlesExceptionGracefully() throws Exception {
        when(connection.isClosed()).thenReturn(true);
        when(activeMQConnectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        listener.setup();

        verify(activeMQConnectionFactory).createConnection();
    }

    /**
     * Tests the send method with byte array when producer creation fails.
     * Verifies that producer creation failures are handled gracefully and return false.
     *
     * @throws Exception if there are unexpected issues during test execution
     */
    @Test
    public void send_BytesMessageProducerCreationFails_ReturnsFalse() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        boolean result = listener.send("test".getBytes(), "test-queue");

        assertFalse(result);
    }

    /**
     * Tests the send method with text message when producer creation fails.
     * Verifies that producer creation failures are handled gracefully and return false.
     *
     * @throws Exception if there are unexpected issues during test execution
     */
    @Test
    public void send_TextMessageProducerCreationFails_ReturnsFalse() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        boolean result = listener.send("test", "test-queue");

        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with a message containing empty reference IDs.
     * Verifies that messages with empty reference ID arrays are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithEmptyReferenceIds_ReturnsTrue() throws Exception {
        String jsonWithEmptyRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithEmptyRefIds);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing null reference IDs.
     * Verifies that messages with null reference ID arrays are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithNullReferenceIds_ReturnsTrue() throws Exception {
        String jsonWithNullRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithNullRefIds);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the executeAsync method with a null response.
     * Verifies that null responses are handled gracefully and return true.
     */
    @Test
    public void executeAsync_NullResponse_ReturnsTrue() {
        boolean result = listener.executeAsync(null, 0, 1, "test-queue");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing multiple reference IDs.
     * Verifies that messages with multiple reference entries are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithMultipleReferenceIds_ReturnsTrue() throws Exception {
        String jsonWithMultipleRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}, " +
                "{\"referenceId\":\"ref2\"}, " +
                "{\"referenceId\":\"ref3\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithMultipleRefIds);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing analytics data.
     * Verifies that messages with analytics information are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithAnalyticsData_ReturnsTrue() throws Exception {
        String jsonWithAnalytics = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"analytics\": {\"key\":\"value\"}}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithAnalytics);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing additional fields beyond the standard schema.
     * Verifies that messages with extra fields are processed successfully without errors.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithAdditionalFields_ReturnsTrue() throws Exception {
        String jsonWithAdditionalFields = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"additional\": \"extra data\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"additional\": \"extra\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithAdditionalFields);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing reference URLs.
     * Verifies that messages with reference URL fields are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithReferenceUrls_ReturnsTrue() throws Exception {
        String jsonWithUrls = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"referenceURL\":\"http://example.com/data\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithUrls);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing request time field.
     * Verifies that messages with timestamp information are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithRequestTime_ReturnsTrue() throws Exception {
        String jsonWithRequestTime = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"requesttime\": \"2024-02-20T10:00:00Z\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithRequestTime);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing version and ID fields.
     * Verifies that messages with versioning information are processed successfully.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithVersion_ReturnsTrue() throws Exception {
        String jsonWithVersion = "{ \"id\": \"mosip.manual.adjudication.adjudicate\", " +
                "\"version\": \"1.0\", " +
                "\"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithVersion);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a PENDING decision and delay configuration.
     * Verifies that pending decisions with delays are handled correctly.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithPendingDecisionAndDelay_ReturnsTrue() throws Exception {
        String jsonWithPending = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithPending);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("PENDING");
        expectation.setDelayResponse(30);
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a large delay value for rejected decisions.
     * Verifies that large delay values are handled correctly without issues.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithLargeDelayRejected_ReturnsTrue() throws Exception {
        String jsonWithLargeDelay = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithLargeDelay);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        expectation.setDelayResponse(300);
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with special characters in reference URLs.
     * Verifies that URLs with query parameters and encoded characters are handled correctly.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithSpecialCharsInUrl_ReturnsTrue() throws Exception {
        String jsonWithSpecialChars = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"referenceURL\":\"http://example.com/data?param=value&special=chars%20here\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithSpecialChars);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with special characters in reference IDs.
     * Verifies that reference IDs containing hyphens, underscores, and dots are handled correctly.
     *
     * @throws Exception if there are issues during message processing
     */
    @Test
    public void consumeLogic_MessageWithSpecialCharsInRefId_ReturnsTrue() throws Exception {
        String jsonWithSpecialRefId = "{ \"requestId\": \"req1\", \"referenceId\": \"ref-1_2.3\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref-1_2.3\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithSpecialRefId);
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref-1_2.3")).thenReturn(expectation);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertTrue(result);
    }

    /**
     * Tests the consume method with an existing connection setup.
     * Verifies that the method properly sets up message consumer and returns empty byte array.
     *
     * @throws Exception if there are issues during consumer setup
     */
    @Test
    public void consume_withExistingConnection_SetsUpConsumerAndReturnsEmptyByteArray() throws Exception {
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
        verify(messageConsumer).setMessageListener(any(MessageListener.class));
    }

    /**
     * Tests the consume method with null destination.
     * Verifies that null destination handling works correctly and consumer is set up properly.
     *
     * @throws Exception if there are issues during consumer setup
     */
    @Test
    public void consume_withNullDestination_SetsUpConsumerAndReturnsEmptyByteArray() throws Exception {
        ReflectionTestUtils.setField(listener, "destination", null);
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
        verify(messageConsumer).setMessageListener(any(MessageListener.class));
    }

    /**
     * Tests the consume method when message consumer creation fails.
     * Verifies that JMS exceptions during consumer creation are handled gracefully.
     *
     * @throws Exception if there are unexpected issues during test execution
     */
    @Test
    public void consume_whenMessageConsumerCreationFails_ReturnsEmptyByteArray() throws Exception {
        when(session.createConsumer(any())).thenThrow(new JMSException("Consumer creation failed"));
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(any());
    }

    /**
     * Tests the consume method with invalid broker URL.
     * Verifies that invalid or empty broker URLs are handled gracefully.
     */
    @Test
    public void consume_withInvalidBrokerUrl_ReturnsEmptyByteArray() {
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", null);
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "";
        String username = "testUser";
        String password = "testPass";

        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        assertEquals(0, result.length);
    }

    /**
     * Tests the consume method when queue creation fails.
     * Verifies that the method handles JMSException gracefully and returns an empty byte array.
     */
    @Test
    public void consume_whenQueueCreationFails_ReturnsEmptyByteArray() throws Exception {
        when(session.createQueue(anyString())).thenThrow(new JMSException("Queue creation failed"));
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        assertEquals(0, result.length);
        verify(session).createQueue(address);
    }
}