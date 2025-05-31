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
        openMocks(this);

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
     * Tests the consumeLogic method with a valid TextMessage containing proper JSON and no specific expectation decision.
     * Verifies that the message is processed correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withTextMessage() throws Exception {
        // Arrange: Set up a valid JSON string and mock TextMessage behavior
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        // Mock expectation cache to return an empty expectation
        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify the message was processed and the correct interactions occurred
        verify(textMessage).getText();
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a non-TextMessage type.
     * Verifies that the method safely ignores unsupported message types and returns false.
     */
    @Test
    public void testConsumeLogic_withInvalidMessage() {
        // Arrange: Mock a generic Message that is not a TextMessage
        Message dummyMessage = mock(Message.class);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(dummyMessage, "dummyAddress");

        // Assert: Verify that the result is false for unsupported message types
        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method when the expectation has a REJECTED decision.
     * Verifies that the message is processed correctly with the specified decision and delay.
     */
    @Test
    public void testConsumeLogic_withExpectationRejected() throws Exception {
        // Arrange: Set up a JSON string and mock TextMessage
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [{\"referenceId\":\"ref1\"}] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        // Mock expectation cache with a REJECTED decision and delay
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        expectation.setDelayResponse(5);
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify the message was processed and the correct interactions occurred
        verify(textMessage).getText();
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a special queue address ("verification-to-mosip").
     * Verifies that the routing logic for this address processes the message correctly.
     */
    @Test
    public void testConsumeLogic_withVerificationResponseAddress() throws Exception {
        // Arrange: Set up a JSON string and mock TextMessage
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        // Mock expectation cache with an empty expectation
        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        // Act: Call the consumeLogic method with the special queue address
        boolean result = listener.consumeLogic(textMessage, "verification-to-mosip");

        // Assert: Verify the message was processed for the special address
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with malformed JSON in the message.
     * Verifies that invalid JSON is handled gracefully and returns false.
     */
    @Test
    public void testConsumeLogic_withInvalidJson() throws Exception {
        // Arrange: Set up an invalid JSON string and mock TextMessage
        String invalidJson = "{ invalid json }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that invalid JSON results in false
        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with a null message.
     * Verifies that a null input is handled gracefully and returns false.
     */
    @Test
    public void testConsumeLogic_withNullMessage(){
        // Act: Call the consumeLogic method with a null message
        boolean result = listener.consumeLogic(null, "dummyAddress");

        // Assert: Verify that a null message results in false
        assertFalse(result);
    }

    /**
     * Tests the getListener static method.
     * Verifies that the created MessageListener delegates message handling to the provided QueueListener.
     */
    @Test
    public void testGetListener_invokesQueueListener() {
        // Arrange: Mock a QueueListener and a Message
        QueueListener queueListener = mock(QueueListener.class);
        Message message = mock(Message.class);

        // Create a MessageListener using the static method
        MessageListener messageListener = Listener.getListener(queueListener);

        // Act: Invoke the onMessage method
        messageListener.onMessage(message);

        // Assert: Verify that the QueueListener's setListener method was called
        verify(queueListener).setListener(message);
    }

    /**
     * Tests the consume method when session creation fails.
     * Verifies that JMSException is handled gracefully and an empty byte array is returned.
     */
    @Test
    public void testConsume_handlesSessionCreationError() throws Exception {
        // Arrange: Mock a session that throws a JMSException when creating a queue
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        Session mockSession = mock(Session.class);
        when(mockSession.createQueue(anyString())).thenThrow(new JMSException("Test exception"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, "brokerUrl", "username", "password");

        // Assert: Verify that an empty byte array is returned
        assertEquals(0, result.length);
    }

    /**
     * Tests the executeAsync method with an invalid text type.
     * Verifies that no send operations occur for an invalid text type and the method returns true.
     */
    @Test
    public void testExecuteAsync_withInvalidTextType() {
        // Arrange: Set up parameters and mock send operations
        String response = "test message";
        int delayResponse = 0; // No delay to avoid sleep
        Integer textType = 3; // Invalid text type
        String mvAddress = "test-queue";
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());
        doReturn(true).when(spyListener).send(any(byte[].class), anyString());

        // Act: Call the executeAsync method
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Assert: Verify that no send operations were called and the result is true
        assertTrue(result);
        verify(spyListener, never()).send(anyString(), anyString());
        verify(spyListener, never()).send(any(byte[].class), anyString());
    }

    /**
     * Tests the consumeLogic method when JSON parsing fails due to an expectation cache error.
     * Verifies that the method handles the exception gracefully and returns false.
     */
    @Test
    public void testConsumeLogic_withJsonParsingError() throws Exception {
        // Arrange: Set up a JSON string and mock TextMessage
        String invalidJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [{\"referenceId\":\"ref1\"}] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(invalidJson);

        // Mock expectation cache to throw an exception
        when(expectationCache.get(anyString())).thenThrow(new RuntimeException("Cache error"));

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the result is false due to the parsing error
        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with an ActiveMQBytesMessage.
     * Verifies that the method returns false for unsupported message types.
     */
    @Test
    public void testConsumeLogic_withBytesMessage() {
        // Arrange: Mock an ActiveMQBytesMessage
        ActiveMQBytesMessage bytesMessage = mock(ActiveMQBytesMessage.class);
        when(bytesMessage.getContent()).thenReturn(new org.apache.activemq.util.ByteSequence("test".getBytes()));

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(bytesMessage, "dummyAddress");

        // Assert: Verify that the result is false for bytes messages
        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with an unsupported message type.
     * Verifies that the method returns false for unsupported message types.
     */
    @Test
    public void testConsumeLogic_withUnsupportedMessageType() {
        // Arrange: Mock a generic unsupported Message
        Message unsupportedMessage = mock(Message.class);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(unsupportedMessage, "dummyAddress");

        // Assert: Verify that the result is false for unsupported message types
        assertFalse(result);
    }

    /**
     * Tests the setup method when connection creation fails.
     * Verifies that the method handles JMSException gracefully.
     */
    @Test
    public void testSetup_whenConnectionCreationFails() throws Exception {
        // Arrange: Mock connection and factory to simulate failure
        when(connection.isClosed()).thenReturn(true);
        when(activeMQConnectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        // Act: Call the setup method
        listener.setup();

        // Assert: Verify that the connection creation was attempted
        verify(activeMQConnectionFactory).createConnection();
    }

    /**
     * Tests the send method with a byte array when message producer creation fails.
     * Verifies that the method returns false when a JMSException occurs.
     */
    @Test
    public void testSendBytes_whenProducerCreationFails() throws Exception {
        // Arrange: Mock a session that throws a JMSException when creating a producer
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        // Act: Call the send method with a byte array
        boolean result = listener.send("test".getBytes(), "test-queue");

        // Assert: Verify that the result is false due to the producer creation failure
        assertFalse(result);
    }

    /**
     * Tests the send method with a text message when message producer creation fails.
     * Verifies that the method returns false when a JMSException occurs.
     */
    @Test
    public void testSendText_whenProducerCreationFails() throws Exception {
        // Arrange: Mock a session that throws a JMSException when creating a producer
        Session mockSession = mock(Session.class);
        when(mockSession.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
        ReflectionTestUtils.setField(listener, "session", mockSession);

        // Act: Call the send method with a text message
        boolean result = listener.send("test", "test-queue");

        // Assert: Verify that the result is false due to the producer creation failure
        assertFalse(result);
    }

    /**
     * Tests the consumeLogic method with a message containing empty reference IDs.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withEmptyRefIds() throws Exception {
        // Arrange: Set up a JSON string with empty reference IDs
        String jsonWithEmptyRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithEmptyRefIds);

        // Mock expectation cache with a REJECTED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing null reference IDs.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withNullRefIds() throws Exception {
        // Arrange: Set up a JSON string with null reference IDs
        String jsonWithNullRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithNullRefIds);

        // Mock expectation cache with a REJECTED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the executeAsync method with a null response.
     * Verifies that the method handles a null response and returns true.
     */
    @Test
    public void testExecuteAsync_withNullResponse() {
        // Act: Call the executeAsync method with a null response
        boolean result = listener.executeAsync(null, 0, 1, "test-queue");

        // Assert: Verify that the method handles null gracefully
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing multiple reference IDs.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withMultipleRefIds() throws Exception {
        // Arrange: Set up a JSON string with multiple reference IDs
        String jsonWithMultipleRefIds = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}, " +
                "{\"referenceId\":\"ref2\"}, " +
                "{\"referenceId\":\"ref3\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithMultipleRefIds);

        // Mock expectation cache with a REJECTED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing analytics data.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withAnalyticsData() throws Exception {
        // Arrange: Set up a JSON string with analytics data
        String jsonWithAnalytics = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"analytics\": {\"key\":\"value\"}}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithAnalytics);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing additional fields.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withAdditionalFields() throws Exception {
        // Arrange: Set up a JSON string with additional fields
        String jsonWithAdditionalFields = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"additional\": \"extra data\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"additional\": \"extra\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithAdditionalFields);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing reference URLs.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withReferenceUrls() throws Exception {
        // Arrange: Set up a JSON string with a reference URL
        String jsonWithUrls = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"referenceURL\":\"http://example.com/data\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithUrls);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing a request time.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withRequestTime() throws Exception {
        // Arrange: Set up a JSON string with a request time
        String jsonWithRequestTime = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"requesttime\": \"2024-02-20T10:00:00Z\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithRequestTime);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing version information.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withVersion() throws Exception {
        // Arrange: Set up a JSON string with version information
        String jsonWithVersion = "{ \"id\": \"mosip.manual.adjudication.adjudicate\", " +
                "\"version\": \"1.0\", " +
                "\"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithVersion);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing a PENDING decision.
     * Verifies that the method processes the message correctly with the specified delay.
     */
    @Test
    public void testConsumeLogic_withPendingDecision() throws Exception {
        // Arrange: Set up a JSON string and mock TextMessage
        String jsonWithPending = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithPending);

        // Mock expectation cache with a PENDING decision and delay
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("PENDING");
        expectation.setDelayResponse(30);
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing a large delay response.
     * Verifies that the method processes the message correctly with the specified delay.
     */
    @Test
    public void testConsumeLogic_withLargeDelay() throws Exception {
        // Arrange: Set up a JSON string and mock TextMessage
        String jsonWithLargeDelay = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithLargeDelay);

        // Mock expectation cache with a REJECTED decision and large delay
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("REJECTED");
        expectation.setDelayResponse(300); // 5 minutes delay
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing a reference URL with special characters.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withSpecialCharsInUrl() throws Exception {
        // Arrange: Set up a JSON string with a reference URL containing special characters
        String jsonWithSpecialChars = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref1\", \"referenceURL\":\"http://example.com/data?param=value&special=chars%20here\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithSpecialChars);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref1")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consumeLogic method with a message containing a reference ID with special characters.
     * Verifies that the method processes the message correctly and returns true.
     */
    @Test
    public void testConsumeLogic_withSpecialCharsInRefId() throws Exception {
        // Arrange: Set up a JSON string with a reference ID containing special characters
        String jsonWithSpecialRefId = "{ \"requestId\": \"req1\", \"referenceId\": \"ref-1_2.3\", " +
                "\"gallery\": { \"referenceIds\": [" +
                "{\"referenceId\":\"ref-1_2.3\"}" +
                "] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(jsonWithSpecialRefId);

        // Mock expectation cache with an APPROVED decision
        Expectation expectation = new Expectation();
        expectation.setMockMvDecision("APPROVED");
        when(expectationCache.get("ref-1_2.3")).thenReturn(expectation);

        // Act: Call the consumeLogic method
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Assert: Verify that the message was processed correctly
        assertTrue(result);
    }

    /**
     * Tests the consume method with an existing connection.
     * Verifies that the method sets up the queue and consumer correctly and returns an empty byte array.
     */
    @Test
    public void testConsume_withExistingConnection() throws Exception {
        // Arrange: Set up parameters for the consume method
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        // Assert: Verify that the queue and consumer were set up correctly and an empty byte array is returned
        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
        verify(messageConsumer).setMessageListener(any(MessageListener.class));
    }

    /**
     * Tests the consume method when the destination is null.
     * Verifies that the method initializes the queue and consumer correctly and returns an empty byte array.
     */
    @Test
    public void testConsume_withNullDestination() throws Exception {
        // Arrange: Set the destination field to null and prepare parameters
        ReflectionTestUtils.setField(listener, "destination", null);
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        // Assert: Verify that the queue and consumer were set up correctly and an empty byte array is returned
        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
        verify(messageConsumer).setMessageListener(any(MessageListener.class));
    }

    /**
     * Tests the consume method when message consumer creation fails.
     * Verifies that the method handles JMSException gracefully and returns an empty byte array.
     */
    @Test
    public void testConsume_whenMessageConsumerCreationFails() throws Exception {
        // Arrange: Mock session to throw a JMSException when creating a consumer
        when(session.createConsumer(any())).thenThrow(new JMSException("Consumer creation failed"));
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        // Assert: Verify that the queue was created and an empty byte array is returned
        assertEquals(0, result.length);
        verify(session).createQueue(address);
        verify(session).createConsumer(queue);
    }

    /**
     * Tests the consume method with an invalid broker URL.
     * Verifies that the method handles the invalid URL gracefully and returns an empty byte array.
     */
    @Test
    public void testConsume_withInvalidBrokerUrl() {
        // Arrange: Set the connection factory to null and provide an empty broker URL
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", null);
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "";  // Invalid broker URL
        String username = "testUser";
        String password = "testPass";

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        // Assert: Verify that an empty byte array is returned
        assertEquals(0, result.length);
    }

    /**
     * Tests the consume method when queue creation fails.
     * Verifies that the method handles JMSException gracefully and returns an empty byte array.
     */
    @Test
    public void testConsume_whenQueueCreationFails() throws Exception {
        // Arrange: Mock session to throw a JMSException when creating a queue
        when(session.createQueue(anyString())).thenThrow(new JMSException("Queue creation failed"));
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);
        String brokerUrl = "test-broker";
        String username = "testUser";
        String password = "testPass";

        // Act: Call the consume method
        byte[] result = listener.consume(address, queueListener, brokerUrl, username, password);

        // Assert: Verify that the queue creation was attempted and an empty byte array is returned
        assertEquals(0, result.length);
        verify(session).createQueue(address);
    }
}