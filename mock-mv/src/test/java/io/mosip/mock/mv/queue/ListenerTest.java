package io.mosip.mock.mv.queue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.anyInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.mosip.mock.mv.dto.Expectation;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Queue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

class ListenerTest {

    private Listener listener;
    private Environment env;
    private io.mosip.mock.mv.service.ExpectationCache expectationCache;

    /**
     * Sets up mocks and injects them into the Listener instance before each test.
     * Also spies on the listener to stub async execution and mock environment property retrieval.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new Listener();

        env = mock(Environment.class);
        expectationCache = mock(io.mosip.mock.mv.service.ExpectationCache.class);
        ReflectionTestUtils.setField(listener, "env", env);
        ReflectionTestUtils.setField(listener, "expectationCache", expectationCache);
        ReflectionTestUtils.setField(listener, "mockDecision", "APPROVED");

        Listener spyListener = org.mockito.Mockito.spy(listener);
        doReturn(true).when(spyListener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
        listener = spyListener;

        when(env.getProperty(Listener.DECISION_SERVICE_ID)).thenReturn("decisionServiceId");
    }

    /**
     * Verifies that a valid TextMessage with proper JSON and no expectation decision processes correctly.
     */
    @Test
    void testConsumeLogic_withTextMessage() throws Exception {
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
    void testConsumeLogic_withInvalidMessage() throws Exception {
        Message dummyMessage = mock(Message.class);

        boolean result = listener.consumeLogic(dummyMessage, "dummyAddress");
        assertFalse(result);
    }

    /**
     * Tests behavior when the expectation's mock decision is REJECTED.
     */
    @Test
    void testConsumeLogic_withExpectationRejected() throws Exception {
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
    void testConsumeLogic_withVerificationResponseAddress() throws Exception {
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
    void testConsumeLogic_withInvalidJson() throws Exception {
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
    void testConsumeLogic_withNullMessage() throws Exception {
        boolean result = listener.consumeLogic(null, "dummyAddress");

        assertFalse(result);
    }

    /**
     * Tests the setup logic when the existing JMS connection is closed,
     * ensuring a new connection and session are created properly.
     */
    @Test
    void testSetup_connectionClosed() throws Exception {
        ActiveMQConnection mockConnection = mock(ActiveMQConnection.class);
        when(mockConnection.isClosed()).thenReturn(true);
        ReflectionTestUtils.setField(listener, "connection", mockConnection);

        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        Connection newConnection = mock(Connection.class);
        Session newSession = mock(Session.class);
        when(mockFactory.createConnection()).thenReturn(newConnection);
        when(newConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(newSession);
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        listener.setup();

        verify(mockFactory).createConnection();
        verify(newConnection).start();
        verify(newConnection).createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Tests consume method with an existing connection.
     * Verifies that a queue consumer is created and message listener is set properly
     * when consuming messages from a specified queue address.
     */
    @Test
    void testConsume_withExistingConnection() throws Exception {
        String address = "test-queue";
        QueueListener queueListener = mock(QueueListener.class);

        // Setup existing connection factory
        ActiveMQConnectionFactory mockFactory = mock(ActiveMQConnectionFactory.class);
        ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", mockFactory);

        // Mock session and queue
        Session mockSession = mock(Session.class);
        ReflectionTestUtils.setField(listener, "session", mockSession);
        Queue mockQueue = mock(Queue.class);  // Mock Queue instead of Destination
        MessageConsumer mockConsumer = mock(MessageConsumer.class);

        when(mockSession.createQueue(address)).thenReturn(mockQueue);
        when(mockSession.createConsumer(any(Queue.class))).thenReturn(mockConsumer);

        // Execute consume method
        byte[] result = listener.consume(address, queueListener, "brokerUrl", "username", "password");

        // Verify consumer was created and listener was set
        verify(mockSession).createQueue(address);
        verify(mockSession).createConsumer(mockQueue);
        verify(mockConsumer).setMessageListener(any(MessageListener.class));
        assertEquals(0, result.length);
    }

    /**
     * Tests the getListener static method.
     * Verifies that the created MessageListener properly delegates
     * message handling to the provided QueueListener.
     */
    @Test
    void testGetListener_invokesQueueListener() {
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
    void testConsume_handlesSessionCreationError() throws Exception {
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
     * Tests async execution with text type 1 (String message).
     * Verifies that the message is sent with proper delay and the correct send method
     * is called for String type messages.
     */
    @Test
    void testExecuteAsync_withTextType1() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 1;
        Integer textType = 1;
        String mvAddress = "test-queue";

        // Mock the send method calls
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Initial verification
        assertTrue(result);

        // Wait for the scheduled task to complete
        Thread.sleep(2000); // Wait for 2 seconds to ensure the task completes

        // Verify the correct send method was called
        verify(spyListener).send(response, mvAddress);
    }

    /**
     * Tests async execution with text type 2 (byte array message).
     * Verifies that the message is sent with proper delay and the correct send method
     * is called for byte array type messages.
     */
    @Test
    void testExecuteAsync_withTextType2() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 1;
        Integer textType = 2;
        String mvAddress = "test-queue";

        // Mock the send method calls
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(any(byte[].class), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Initial verification
        assertTrue(result);

        // Wait for the scheduled task to complete
        Thread.sleep(2000);

        // Verify the correct send method was called
        verify(spyListener).send(response.getBytes(), mvAddress);
    }

    /**
     * Tests async execution with zero delay.
     * Verifies that messages are sent immediately when delay is set to zero,
     * without waiting for scheduled execution.
     */
    @Test
    void testExecuteAsync_withZeroDelay() throws Exception {
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

        // Initial verification
        assertTrue(result);

        // Wait for a short time to ensure the task completes
        Thread.sleep(500);

        // Verify the send method was called
        verify(spyListener).send(response, mvAddress);
    }

    /**
     * Tests error handling in async execution when send fails.
     * Verifies that exceptions during message sending are handled properly
     * and don't prevent the method from completing.
     */
    @Test
    void testExecuteAsync_whenSendThrowsException() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 1;
        Integer textType = 1;
        String mvAddress = "test-queue";

        // Mock the send method to throw exception
        Listener spyListener = spy(listener);
        doThrow(new RuntimeException("Send failed")).when(spyListener).send(anyString(), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Initial verification
        assertTrue(result);

        // Wait for the scheduled task to complete
        Thread.sleep(2000);

        // Verify the send method was called and exception was logged
        verify(spyListener).send(response, mvAddress);
    }

    /**
     * Tests async execution with invalid text type.
     * Verifies that no send methods are called when an invalid text type
     * is specified (neither type 1 nor type 2).
     */
    @Test
    void testExecuteAsync_withInvalidTextType() throws Exception {
        // Setup
        String response = "test message";
        int delayResponse = 1;
        Integer textType = 3; // Invalid type
        String mvAddress = "test-queue";

        // Mock the send method calls
        Listener spyListener = spy(listener);
        doReturn(true).when(spyListener).send(anyString(), anyString());
        doReturn(true).when(spyListener).send(any(byte[].class), anyString());

        // Execute
        boolean result = spyListener.executeAsync(response, delayResponse, textType, mvAddress);

        // Initial verification
        assertTrue(result);

        // Wait for the scheduled task to complete
        Thread.sleep(2000);

        // Verify neither send method was called
        verify(spyListener, never()).send(anyString(), anyString());
        verify(spyListener, never()).send(any(byte[].class), anyString());
    }
}
