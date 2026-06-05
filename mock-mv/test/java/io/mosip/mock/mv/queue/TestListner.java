package io.mosip.mock.mv.queue;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.util.ByteSequence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;

/**
 * Comprehensive test class for {@link Listener} with enhanced coverage.
 * Tests all methods, message types, and edge cases.
 */
@RunWith(SpringRunner.class)
public class TestListner {
	@Mock
	private ExpectationCache expectationCache;

	@Mock
	private ActiveMQConnectionFactory activeMQConnectionFactory;

	@Mock
	private Session session;

	@Mock
	private Environment env;

	@Mock
	private ActiveMQConnection connection;

	@Mock
	private ConnectionFactory connectionFactory;

	@Mock
	private MessageProducer messageProducer;

	@Mock
	private MessageConsumer messageConsumer;

	@Mock
	private Queue queue;

	@Mock
	private BytesMessage bytesMessage;

	@Mock
	private TextMessage textMessage;

	@Spy
	@InjectMocks
	private Listener listener;

	private ActiveMQTextMessage activeMQTextMessage;
	private ActiveMQBytesMessage activeMQBytesMessage;
	private String validJsonString;
	private String invalidJsonString;

	/**
	 * Setup method to initialize test data and mocks.
	 *
	 * @throws Exception if an error occurs during setup.
	 */
	@Before
	public void setup() throws Exception {
		MockitoAnnotations.openMocks(this);

		// Setup valid JSON string
		validJsonString = "{\n" + "  \"id\" : \"mosip.manual.adjudication.adjudicate\",\n" + "  \"version\" : \"1.0\",\n"
				+ "  \"requestId\" : \"d9a75df6-1b96-4f61-934c-b705c1409e81\",\n"
				+ "  \"referenceId\" : \"10002100800001020230223050340\",\n"
				+ "  \"requesttime\" : \"2023-07-17T12:27:25.971Z\",\n"
				+ "  \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/test\",\n"
				+ "  \"addtional\" : null,\n" + "  \"gallery\" : {\n" + "    \"referenceIds\" : [ {\n"
				+ "      \"referenceId\" : \"10002100800000920230221130731\",\n"
				+ "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/test1\"\n"
				+ "    }, {\n" + "      \"referenceId\" : \"10007100090003520230222093240\",\n"
				+ "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/test2\"\n"
				+ "    } ]\n" + "  }\n" + "}";

		// Setup invalid JSON string
		invalidJsonString = "{invalid json}";

		// Setup ActiveMQ messages
		activeMQTextMessage = new ActiveMQTextMessage();
		activeMQTextMessage.setText(validJsonString);

		activeMQBytesMessage = mock(ActiveMQBytesMessage.class);
		when(activeMQBytesMessage.getContent()).thenReturn(new ByteSequence(validJsonString.getBytes()));

		// Setup common mocks
		when(env.getProperty(Listener.DECISION_SERVICE_ID)).thenReturn("test.decision.service.id");
		when(connection.isClosed()).thenReturn(false);
		when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
		when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		when(session.createQueue(anyString())).thenReturn(queue);
		when(session.createConsumer(any(Queue.class))).thenReturn(messageConsumer);
		when(session.createProducer(any(Queue.class))).thenReturn(messageProducer);
		when(session.createTextMessage(anyString())).thenReturn(textMessage);
		when(session.createBytesMessage()).thenReturn(bytesMessage);

		doNothing().when(connection).start();
		doNothing().when(messageConsumer).setMessageListener(any(MessageListener.class));
		doNothing().when(messageProducer).send(any(Message.class));
		doNothing().when(bytesMessage).writeObject(any());

		// Setup reflection fields
		ReflectionTestUtils.setField(listener, "env", env);
		ReflectionTestUtils.setField(listener, "expectationCache", expectationCache);
		ReflectionTestUtils.setField(listener, "mockDecision", "APPROVED");
		ReflectionTestUtils.setField(listener, "isSuccess", true);
		ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", activeMQConnectionFactory);
		ReflectionTestUtils.setField(listener, "connection", connection);
		ReflectionTestUtils.setField(listener, "session", session);
		ReflectionTestUtils.setField(listener, "verificationResponseAddress", "verification-to-mosip");

		// Mock executeAsync to return true
		doReturn(true).when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with valid ActiveMQTextMessage.
	 *
	 * @throws Exception if an error occurs during the test.
	 */
	@Test
	public void testConsumeLogic_ValidActiveMQTextMessage_ReturnsTrue() throws Exception {
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation("10002100800001020230223050340", "PENDING", 30));

		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");

		assertTrue(result);
		verify(expectationCache).get("10002100800001020230223050340");
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with REJECTED decision.
	 */
	@Test
	public void testConsumeLogic_RejectedDecision_ReturnsTrue() throws Exception {
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation("10002100800001020230223050340", "REJECTED", 60));

		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");

		assertTrue(result);
		verify(listener).executeAsync(anyString(), eq(60), eq(1), eq("adjudication-to-mosip"));
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with APPROVED decision.
	 */
	@Test
	public void testConsumeLogic_ApprovedDecision_ReturnsTrue() throws Exception {
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation("10002100800001020230223050340", "APPROVED", 0));

		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");

		assertTrue(result);
		verify(listener).executeAsync(anyString(), eq(0), eq(1), eq("adjudication-to-mosip"));
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with verification response address.
	 */
	@Test
	public void testConsumeLogic_VerificationResponseAddress_ReturnsTrue() throws Exception {
		boolean result = listener.consumeLogic(activeMQTextMessage, "verification-to-mosip");

		assertTrue(result);
		verify(listener).executeAsync(anyString(), eq(0), eq(1), eq("verification-to-mosip"));
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with ActiveMQBytesMessage.
	 */
	@Test
	public void testConsumeLogic_ActiveMQBytesMessage_ReturnsFalse() throws Exception {
		// ActiveMQBytesMessage returns byte array string representation which is invalid JSON
		boolean result = listener.consumeLogic(activeMQBytesMessage, "adjudication-to-mosip");

		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with regular TextMessage.
	 */
	@Test
	public void testConsumeLogic_RegularTextMessage_ReturnsTrue() throws Exception {
		when(textMessage.getText()).thenReturn(validJsonString);
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation());

		boolean result = listener.consumeLogic(textMessage, "adjudication-to-mosip");

		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with invalid JSON.
	 */
	@Test
	public void testConsumeLogic_InvalidJson_ReturnsFalse() throws Exception {
		ActiveMQTextMessage invalidMessage = new ActiveMQTextMessage();
		invalidMessage.setText(invalidJsonString);

		boolean result = listener.consumeLogic(invalidMessage, "adjudication-to-mosip");

		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with unsupported message type.
	 */
	@Test
	public void testConsumeLogic_UnsupportedMessageType_ReturnsFalse() {
		Message unsupportedMessage = mock(Message.class);

		boolean result = listener.consumeLogic(unsupportedMessage, "adjudication-to-mosip");

		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with null message.
	 */
	@Test
	public void testConsumeLogic_NullMessage_ReturnsFalse() {
		boolean result = listener.consumeLogic(null, "adjudication-to-mosip");
		
		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with cache exception.
	 */
	@Test
	public void testConsumeLogic_CacheException_ReturnsFalse() throws Exception {
		when(expectationCache.get(anyString())).thenThrow(new RuntimeException("Cache error"));
		
		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");
		
		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with default mock decision.
	 */
	@Test
	public void testConsumeLogic_DefaultMockDecision_UsesDefaultBehavior() throws Exception {
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation()); // Empty expectation
		ReflectionTestUtils.setField(listener, "mockDecision", "REJECTED");
		
		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");
		
		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#setup()}.
	 */
	@Test
	public void testSetup_NullConnection_CreatesNewConnection() throws Exception {
		ReflectionTestUtils.setField(listener, "connection", null);
		ReflectionTestUtils.setField(listener, "session", null);
		
		listener.setup();
		
		verify(activeMQConnectionFactory).createConnection();
		verify(connection).start();
		verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	/**
	 * Test method for {@link Listener#setup()} with closed connection.
	 */
	@Test
	public void testSetup_ClosedConnection_CreatesNewConnection() throws Exception {
		when(connection.isClosed()).thenReturn(true);
		
		listener.setup();
		
		verify(activeMQConnectionFactory).createConnection();
	}

	/**
	 * Test method for {@link Listener#setup()} with connection creation failure.
	 */
	@Test
	public void testSetup_ConnectionCreationFails_HandlesException() throws Exception {
		ReflectionTestUtils.setField(listener, "connection", null);
		when(activeMQConnectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));
		
		// Should not throw exception
		listener.setup();
		
		verify(activeMQConnectionFactory).createConnection();
	}

	/**
	 * Test method for {@link Listener#runAdjudicationQueue()}.
	 */
	@Test
	public void testRunAdjudicationQueue_ProperSetup_SetsUpListener() {
		doReturn(new byte[0]).when(listener).consume(anyString(), any(QueueListener.class), anyString(), anyString(), anyString());
		ReflectionTestUtils.setField(listener, "mvRequestAddress", "mosip-to-adjudication");
		ReflectionTestUtils.setField(listener, "mvResponseAddress", "adjudication-to-mosip");
		ReflectionTestUtils.setField(listener, "mabrokerUrl", "tcp://localhost:61616");
		ReflectionTestUtils.setField(listener, "mausername", "admin");
		ReflectionTestUtils.setField(listener, "mapassword", "admin");
		
		listener.runAdjudicationQueue();
		
		verify(listener).consume(eq("mosip-to-adjudication"), any(QueueListener.class), eq("tcp://localhost:61616"), eq("admin"), eq("admin"));
	}

	/**
	 * Test method for {@link Listener#runVerificationQueue()}.
	 */
	@Test
	public void testRunVerificationQueue_ProperSetup_SetsUpListener() {
		doReturn(new byte[0]).when(listener).consume(anyString(), any(QueueListener.class), anyString(), anyString(), anyString());
		ReflectionTestUtils.setField(listener, "verificationRequestAddress", "mosip-to-verification");
		ReflectionTestUtils.setField(listener, "verificationResponseAddress", "verification-to-mosip");
		ReflectionTestUtils.setField(listener, "vbrokerUrl", "tcp://localhost:61616");
		ReflectionTestUtils.setField(listener, "vusername", "admin");
		ReflectionTestUtils.setField(listener, "vpassword", "admin");
		
		listener.runVerificationQueue();
		
		verify(listener).consume(eq("mosip-to-verification"), any(QueueListener.class), eq("tcp://localhost:61616"), eq("admin"), eq("admin"));
	}

	/**
	 * Test method for {@link Listener#consume(String, QueueListener, String, String, String)}.
	 */
	@Test
	public void testConsume_ProperSetup_ReturnsEmptyByteArray() throws Exception {
		ReflectionTestUtils.setField(listener, "activeMQConnectionFactory", null);
		QueueListener queueListener = mock(QueueListener.class);
		
		byte[] result = listener.consume("test-queue", queueListener, "tcp://localhost:61616", "admin", "admin");
		
		assertEquals(0, result.length);
	}

	/**
	 * Test method for {@link Listener#getListener(QueueListener)}.
	 */
	@Test
	public void testGetListener_ProperDelegation_DelegatesToQueueListener() {
		QueueListener queueListener = mock(QueueListener.class);
		Message message = mock(Message.class);
		
		MessageListener messageListener = Listener.getListener(queueListener);
		messageListener.onMessage(message);
		
		verify(queueListener).setListener(message);
	}

	/**
	 * Test method for {@link Listener#send(byte[], String)}.
	 */
	@Test
	public void testSendBytes_SuccessfulSending_ReturnsTrue() throws Exception {
		byte[] messageBytes = "test message".getBytes();
		
		Boolean result = listener.send(messageBytes, "test-queue");
		
		assertTrue(result);
		verify(session).createQueue("test-queue");
		verify(session).createProducer(queue);
		verify(session).createBytesMessage();
	}

	/**
	 * Test method for {@link Listener#send(String, String)}.
	 */
	@Test
	public void testSendText_SuccessfulSending_ReturnsTrue() throws Exception {
		String messageText = "test message";
		
		Boolean result = listener.send(messageText, "test-queue");
		
		assertTrue(result);
		verify(session).createQueue("test-queue");
		verify(session).createProducer(queue);
		verify(session).createTextMessage(messageText);
	}

	/**
	 * Test method for {@link Listener#send(String, String)} with producer creation failure.
	 */
	@Test
	public void testSend_ProducerCreationFails_ReturnsFalse() throws Exception {
		when(session.createProducer(any())).thenThrow(new JMSException("Producer creation failed"));
		
		Boolean result = listener.send("test", "test-queue");
		
		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#objectMapper()}.
	 */
	@Test
	public void testObjectMapper_ProperConfiguration_ReturnsConfiguredMapper() {
		com.fasterxml.jackson.databind.ObjectMapper mapper = Listener.objectMapper();
		
		assertNotNull(mapper);
		assertFalse(mapper.getDeserializationConfig().isEnabled(
				com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
	}

	/**
	 * Test method for {@link Listener#javaObjectToJsonString(Object)}.
	 */
	@Test
	public void testJavaObjectToJsonString_SuccessfulConversion_ReturnsJsonString() throws Exception {
		Expectation expectation = new Expectation("test-rid", "APPROVED", 30);
		
		String jsonString = Listener.javaObjectToJsonString(expectation);
		
		assertNotNull(jsonString);
		assertTrue(jsonString.contains("test-rid"));
		assertTrue(jsonString.contains("APPROVED"));
	}

	/**
	 * Test method for {@link Listener#executeAsync(String, int, Integer, String)} with text type 1.
	 */
	@Test
	public void testExecuteAsync_TextType1_ExecutesCorrectly() {
		doReturn(true).when(listener).send(anyString(), anyString());
		doCallRealMethod().when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
		
		boolean result = listener.executeAsync("test response", 0, 1, "test-queue");
		
		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#executeAsync(String, int, Integer, String)} with text type 2.
	 */
	@Test
	public void testExecuteAsync_TextType2_ExecutesCorrectly() {
		doReturn(true).when(listener).send(any(byte[].class), anyString());
		doCallRealMethod().when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
		
		boolean result = listener.executeAsync("test response", 0, 2, "test-queue");
		
		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#executeAsync(String, int, Integer, String)} with invalid text type.
	 */
	@Test
	public void testExecuteAsync_InvalidTextType_ExecutesWithoutSending() {
		doCallRealMethod().when(listener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
		
		boolean result = listener.executeAsync("test response", 0, 3, "test-queue");
		
		assertTrue(result);
		verify(listener, never()).send(anyString(), anyString());
		verify(listener, never()).send(any(byte[].class), anyString());
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with success flag false.
	 */
	@Test
	public void testConsumeLogic_SuccessFalse_SetsReturnValue2() throws Exception {
		ReflectionTestUtils.setField(listener, "isSuccess", false);
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation());
		
		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");
		
		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with empty reference IDs.
	 */
	@Test
	public void testConsumeLogic_EmptyReferenceIds_HandlesCorrectly() throws Exception {
		String emptyRefIdsJson = "{\"requestId\":\"req123\",\"referenceId\":\"ref123\",\"gallery\":{\"referenceIds\":[]}}";
		ActiveMQTextMessage emptyRefMessage = new ActiveMQTextMessage();
		emptyRefMessage.setText(emptyRefIdsJson);
		
		when(expectationCache.get("ref123")).thenReturn(new Expectation("ref123", "REJECTED", 0));
		
		boolean result = listener.consumeLogic(emptyRefMessage, "adjudication-to-mosip");
		
		assertTrue(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with large delay value.
	 */
	@Test
	public void testConsumeLogic_LargeDelayValue_HandlesCorrectly() throws Exception {
		when(expectationCache.get("10002100800001020230223050340"))
				.thenReturn(new Expectation("10002100800001020230223050340", "REJECTED", 3600));
		
		boolean result = listener.consumeLogic(activeMQTextMessage, "adjudication-to-mosip");
		
		assertTrue(result);
		verify(listener).executeAsync(anyString(), eq(3600), eq(1), eq("adjudication-to-mosip"));
	}

	/**
	 * Test method for {@link Listener#consume(String, QueueListener, String, String, String)} with session failure.
	 */
	@Test
	public void testConsume_SessionOperationsFail_HandlesGracefully() throws Exception {
		when(session.createQueue(anyString())).thenThrow(new JMSException("Queue creation failed"));
		QueueListener queueListener = mock(QueueListener.class);
		
		byte[] result = listener.consume("test-queue", queueListener, "broker", "user", "pass");
		
		assertEquals(0, result.length);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with JMS exception during message processing.
	 */
	@Test
	public void testConsumeLogic_JMSException_ReturnsFalse() throws Exception {
		TextMessage faultyMessage = mock(TextMessage.class);
		when(faultyMessage.getText()).thenThrow(new JMSException("Message processing failed"));
		
		boolean result = listener.consumeLogic(faultyMessage, "adjudication-to-mosip");
		
		assertFalse(result);
	}

	/**
	 * Test method for {@link Listener#consumeLogic(Message, String)} with multiple reference IDs.
	 */
	@Test
	public void testConsumeLogic_MultipleReferenceIds_HandlesCorrectly() throws Exception {
		String multipleRefIdsJson = "{\"requestId\":\"req123\",\"referenceId\":\"ref123\",\"gallery\":{\"referenceIds\":[{\"referenceId\":\"ref1\"},{\"referenceId\":\"ref2\"},{\"referenceId\":\"ref3\"}]}}";
		ActiveMQTextMessage multipleRefMessage = new ActiveMQTextMessage();
		multipleRefMessage.setText(multipleRefIdsJson);
		
		when(expectationCache.get("ref123")).thenReturn(new Expectation("ref123", "REJECTED", 0));
		
		boolean result = listener.consumeLogic(multipleRefMessage, "adjudication-to-mosip");
		
		assertTrue(result);
	}
}