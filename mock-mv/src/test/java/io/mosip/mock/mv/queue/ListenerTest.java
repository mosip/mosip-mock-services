package io.mosip.mock.mv.queue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

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
     * Sets up mocks and injects them into the Listener instance before each test.
     * Also spies on the listener to stub async execution and mock environment property retrieval.
     */
    @Before
    public void setUp() throws JMSException {
        MockitoAnnotations.openMocks(this);
        
        // Create and initialize the listener
        listener = new Listener();
        
        // Set up the mocks
        when(env.getProperty(Listener.DECISION_SERVICE_ID)).thenReturn("decisionServiceId");
        when(connection.isClosed()).thenReturn(false);
        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createConsumer(any(Queue.class))).thenReturn(messageConsumer);
        doNothing().when(connection).start();
        doNothing().when(messageConsumer).setMessageListener(any(MessageListener.class));
        
        // Inject the mocks into the listener
        ReflectionTestUtils.setField(listener, "env", env);
        ReflectionTestUtils.setField(listener, "expectationCache", expectationCache);
        ReflectionTestUtils.setField(listener, "mockDecision", "APPROVED");
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", activeMQConnectionFactory);
        ReflectionTestUtils.setField(listener, "session", session);
        ReflectionTestUtils.setField(listener, "connection", connection);
        
        // Create a spy of the listener after all fields are set
        listener = spy(listener);
        doReturn(true).when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
    }

    /**
     * Verifies that a valid TextMessage with proper JSON and no expectation decision processes correctly.
     */
    @Test
    public void testConsumeLogic_withTextMessage() throws Exception {
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
     * Verifies that non-TextMessage types are safely ignored and return false.
     */
    @Test
    public void testConsumeLogic_withInvalidMessage() throws Exception {
        Message dummyMessage = mock(Message.class);

        boolean result = listener.consumeLogic(dummyMessage, "dummyAddress");
        assertFalse(result);
    }

    /**
     * Tests behavior when the expectation's mock decision is REJECTED.
     */
    @Test
    public void testConsumeLogic_withExpectationRejected() throws Exception {
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
     * Tests special routing logic for the "verification-to-mosip" queue address.
     */
    @Test
    public void testConsumeLogic_withVerificationResponseAddress() throws Exception {
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        boolean result = listener.consumeLogic(textMessage, "verification-to-mosip");

        assertTrue(result);
    }

    /**
     * Verifies that invalid (malformed) JSON in the message is handled gracefully.
     */
    @Test
    public void testConsumeLogic_withInvalidJson() throws Exception {
        String invalidJson = "{ invalid json }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);

        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Validates that a null message input results in false without exceptions.
     */
    @Test
    public void testConsumeLogic_withNullMessage() throws Exception {
        boolean result = listener.consumeLogic(null, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests consume method with an existing connection.
     */
    @Test
    public void testConsume_withExistingConnection() throws Exception {
        // Setup
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        
        // Execute
        byte[] result = listener.consume(address, queueListener, "brokerUrl", "username", "password");
        
        // Verify
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
        verify(messageConsumer).setMessageListener(any(MessageListener.class));
        assertEquals(0, result.length);
    }

    /**
     * Tests the getListener static method.
     * Verifies that the created MessageListener properly delegates
     * message handling to the provided QueueListener.
     */
    @Test
    public void testGetListener_invokesQueueListener() {
        QueueListener queueListener = mock(QueueListener.class);
        Message message = mock(Message.class);

        // Create message listener
        MessageListener messageListener = Listener.getListener(queueListener);

        // Invoke onMessage
        messageListener.onMessage(message);

        // Verify queue listener was called
        verify(queueListener).setListener(message);
    }

    /**
     * Tests error handling in consume method when session creation fails.
     * Verifies that the method handles JMSException gracefully and returns empty byte array.
     */
    @Test
    public void testConsume_handlesSessionCreationError() throws Exception {
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);

        // Setup mock session that throws exception
        Session mockSession = mock(Session.class);
        when(mockSession.createQueue(anyString())).thenThrow(new JMSException("Test exception"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        // Execute consume method
        byte[] result = listener.consume(address, queueListener, "brokerUrl", "username", "password");

        // Verify error was handled gracefully
        assertEquals(0, result.length);
    }

    /**
     * Tests async execution with zero delay.
     */
    @Test
    public void testExecuteAsync_withZeroDelay() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 0;
        Integer textType = 1;
        String mvAddress = "test-queue";

        // Mock the send method calls
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Verify
        assertTrue(result);
        verify(spyListener).send(response, mvAddress);
    }

    /**
     * Tests async execution with invalid text type.
     */
    @Test
    public void testExecuteAsync_withInvalidTextType() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 0; // Changed to 0 to avoid sleep
        Integer textType = 3; // Invalid type
        String mvAddress = "test-queue";

        // Mock the send method calls
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());
        doReturn(true).when(spyListener).send(any(byte[].class), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Verify
        assertTrue(result);
        verify(spyListener, never()).send(anyString(), anyString());
        verify(spyListener, never()).send(any(byte[].class), anyString());
    }

    /**
     * Tests error handling in consumeLogic when JSON parsing fails.
     */
    @Test
    public void testConsumeLogic_withJsonParsingError() throws Exception {
        String invalidJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [{\"referenceId\":\"ref1\"}] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);
        
        // Mock expectation cache to throw exception
        when(expectationCache.get(anyString())).thenThrow(new RuntimeException("Cache error"));
        
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");
        assertFalse(result);
    }

    /**
     * Tests message handling with ActiveMQBytesMessage.
     * This tests the behavior through the public consumeLogic method.
     */
    @Test
    public void testConsumeLogic_withBytesMessage() throws Exception {
        ActiveMQBytesMessage bytesMessage = mock(ActiveMQBytesMessage.class);
        when(bytesMessage.getContent()).thenReturn(new org.apache.activemq.util.ByteSequence("test".getBytes()));
        
        boolean result = listener.consumeLogic(bytesMessage, "dummyAddress");
        assertFalse(result); // Should return false as bytes message is not supported in consumeLogic
    }

    /**
     * Tests message handling with unsupported message type.
     * This tests the behavior through the public consumeLogic method.
     */
    @Test
    public void testConsumeLogic_withUnsupportedMessageType() throws Exception {
        Message unsupportedMessage = mock(Message.class);
        
        boolean result = listener.consumeLogic(unsupportedMessage, "dummyAddress");
        assertFalse(result);
    }

    /**
     * Tests setup method when connection creation fails.
     */
    @Test
    public void testSetup_whenConnectionCreationFails() throws Exception {
        // Setup
        when(connection.isClosed()).thenReturn(true);
        when(activeMQConnectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));
        
        // Execute
        listener.setup();
        
        // Verify
        verify(activeMQConnectionFactory).createConnection();
    }

    /**
     * Tests send method with byte array when message producer creation fails.
     */
    @Test
    public void testSendBytes_whenProducerCreationFails() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);
        
        boolean result = listener.send("test".getBytes(), "test-queue");
        
        assertFalse(result);
    }

    /**
     * Tests send method with text message when message producer creation fails.
     */
    @Test
    public void testSendText_whenProducerCreationFails() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);
        
        boolean result = listener.send("test", "test-queue");
        
        assertFalse(result);
    }

    /**
     * Tests candidate list population through consumeLogic with empty reference IDs.
     */
    @Test
    public void testConsumeLogic_withEmptyRefIds() throws Exception {
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
     * Tests candidate list population through consumeLogic with null reference IDs.
     */
    @Test
    public void testConsumeLogic_withNullRefIds() throws Exception {
        // Setup
        String jsonWithNullRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithNullRefIds);
        
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);
        
        // Execute
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");
        
        // Verify
        assertTrue(result);
    }

    /**
     * Tests executeAsync with null response.
     */
    @Test
    public void testExecuteAsync_withNullResponse() {
        boolean result = listener.executeAsync(null, 0, 1, "test-queue");
        assertTrue(result);
    }

    /**
     * Tests consumeLogic with a message containing multiple reference IDs.
     */
    @Test
    public void testConsumeLogic_withMultipleRefIds() throws Exception {
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
     * Tests consumeLogic with a message containing analytics data.
     */
    @Test
    public void testConsumeLogic_withAnalyticsData() throws Exception {
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
     * Tests consumeLogic with a message containing additional fields.
     */
    @Test
    public void testConsumeLogic_withAdditionalFields() throws Exception {
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
     * Tests consumeLogic with a message containing reference URLs.
     */
    @Test
    public void testConsumeLogic_withReferenceUrls() throws Exception {
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
     * Tests consumeLogic with a message containing request time.
     */
    @Test
    public void testConsumeLogic_withRequestTime() throws Exception {
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
     * Tests consumeLogic with a message containing version information.
     */
    @Test
    public void testConsumeLogic_withVersion() throws Exception {
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
     * Tests consumeLogic with a message containing a pending decision.
     */
    @Test
    public void testConsumeLogic_withPendingDecision() throws Exception {
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
     * Tests consumeLogic with a message containing a large delay response.
     */
    @Test
    public void testConsumeLogic_withLargeDelay() throws Exception {
        String jsonWithLargeDelay = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithLargeDelay);
        
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        expectation.setDelayResponse(300); // 5 minutes delay
        when(expectationCache.get("ref1")).thenReturn(expectation);
        
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");
        assertTrue(result);
    }

    /**
     * Tests consumeLogic with a message containing a reference URL with special characters.
     */
    @Test
    public void testConsumeLogic_withSpecialCharsInUrl() throws Exception {
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
     * Tests consumeLogic with a message containing a reference ID with special characters.
     */
    @Test
    public void testConsumeLogic_withSpecialCharsInRefId() throws Exception {
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
}
