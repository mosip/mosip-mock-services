package io.mosip.mock.mv.queue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.mosip.mock.mv.constant.MVErrorCode;
import io.mosip.mock.mv.dto.AnalyticsDTO;
import io.mosip.mock.mv.dto.Candidate;
import io.mosip.mock.mv.dto.CandidateList;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.dto.ManualAdjudicationRequestDTO;
import io.mosip.mock.mv.dto.ManualAdjudicationResponseDTO;
import io.mosip.mock.mv.dto.ReferenceIds;
import io.mosip.mock.mv.exception.MVException;
import io.mosip.mock.mv.service.ExpectationCache;
import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

/**
 * Listens to JMS queues for manual adjudication and verification requests, processes them,
 * and sends back appropriate responses asynchronously.
 * <p>
 * This component connects to ActiveMQ brokers based on configuration properties and consumes
 * messages from specified queues. Depending on the message type and content, it determines
 * the response to be sent back. Responses are processed asynchronously using timers, allowing
 * configurable delays before sending them back to the appropriate queue.
 * <p>
 * Uses {@link ExpectationCache} to retrieve mock decision configurations and {@link ObjectMapper}
 * for JSON serialization and deserialization.
 * 
 * @author
 */
@Component
public class Listener {
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	public static final String DECISION_SERVICE_ID = "mosip.registration.processor.manual.verification.decision.id";
	@SuppressWarnings("unused")
	private static final String APPROVED = "APPROVED";
	private static final String REJECTED = "REJECTED";

	@Autowired
	@SuppressWarnings({ "java:S6813" })
	private Environment env;

	/** The username. */
	@Value("${mock.mv.default.decision}")
	private String mockDecision;

	@Value("${mock.mv.success:true}")
	private boolean isSuccess;

	/** The username. */
	@Value("${registration.processor.manual.adjudication.queue.username}")
	private String mausername;

	/** The password. */
	@Value("${registration.processor.manual.adjudication.queue.password}")
	private String mapassword;

	@Value("${registration.processor.manual.adjudication.queue.url}")
	private String mabrokerUrl;

	/** The username. */
	@Value("${registration.processor.verification.queue.username}")
	private String vusername;

	/** The password. */
	@Value("${registration.processor.verification.queue.password}")
	private String vpassword;

	@Value("${registration.processor.verification.queue.url}")
	private String vbrokerUrl;

	/** The type of queue. */
	@Value("${registration.processor.queue.typeOfQueue}")
	private String typeOfQueue;

	/** The address. */
	@Value("${registration.processor.queue.manual.adjudication.response:adjudication-to-mosip}")
	private String mvResponseAddress;

	/** The address. */
	@Value("${registration.processor.queue.manual.adjudication.request:mosip-to-adjudication}")
	private String mvRequestAddress;

	/** The address. */
	@Value("${registration.processor.queue.verification.response:verification-to-mosip}")
	private String verificationResponseAddress;

	/** The address. */
	@Value("${registration.processor.queue.verification.request:mosip-to-verification}")
	private String verificationRequestAddress;

	@Autowired
	@SuppressWarnings({ "java:S6813" })
	private ExpectationCache expectationCache;

	private ActiveMQConnectionFactory activeMQConnectionFactory;

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";
	
	private Connection connection;
	private Session session;
	private Destination destination;

	private Timer timer = new Timer();

	 /**
     * Processes the incoming JMS message, determines the appropriate response based on
     * configured expectations or default decisions, and sends the response asynchronously
     * with a configurable delay.
     * 
     * @param message The JMS message received.
     * @param mvAddress The address of the destination queue for sending the response.
     * @return {@code true} if the response was successfully sent asynchronously; {@code false} otherwise.
     */
	@SuppressWarnings({ "java:S3776" })
	public boolean consumeLogic(jakarta.jms.Message message, String mvAddress) {
		boolean isrequestAddedtoQueue = false;
		int textType = 0;
		StringBuilder messageData = new StringBuilder();
		try {
			textType = checkConsumeInfo(message, messageData);
			if (textType == 0){
				logger.error("Received message is neither text nor byte");
				return false;
			}
			logger.info("Message Data {}", messageData);

			ManualAdjudicationRequestDTO requestDTO = objectMapper().readValue(messageData.toString(),
					ManualAdjudicationRequestDTO.class);
			ManualAdjudicationResponseDTO decisionDto = new ManualAdjudicationResponseDTO();
			decisionDto.setId(env.getProperty(DECISION_SERVICE_ID));
			decisionDto.setRequestId(requestDTO.getRequestId());
			decisionDto.setResponsetime(OffsetDateTime.now().toInstant().toString());
			decisionDto.setReturnValue(isSuccess ? 1 : 2);// logic needs to be implemented.
			int delayResponse = 0;
			if (!mvAddress.equalsIgnoreCase(verificationResponseAddress)) {
				List<ReferenceIds> refIds = requestDTO.getGallery().getReferenceIds();
				CandidateList candidateList = new CandidateList();
				Expectation expectation = expectationCache.get(requestDTO.getReferenceId());
				if (expectation.getMockMvDecision() != null && !expectation.getMockMvDecision().isEmpty()) {
					if (expectation.getMockMvDecision().equalsIgnoreCase(REJECTED)) {
						candidateList = populatesCandidateList(refIds);
					} else {
						candidateList.setCandidates(null);
					}
					delayResponse = (expectation.getDelayResponse() > 0) ? expectation.getDelayResponse() : 0;
				} else {
					if (mockDecision.equalsIgnoreCase(REJECTED)) {
						candidateList = populatesCandidateList(refIds);
					} else
						candidateList.setCandidates(null);
				}
				Map<String, String> analytics = new HashMap<>();
				analytics.put("primaryOperatorID", "110006");// logic needs to be implemented
				analytics.put("primaryOperatorComments", "abcd");
				candidateList
						.setCount(candidateList.getCandidates() != null ? candidateList.getCandidates().size() : 0);
				candidateList.setAnalytics(analytics);
				decisionDto.setCandidateList(candidateList);
			}

			String response = javaObjectToJsonString(decisionDto);
			logger.info("Request type is {}", response);
			isrequestAddedtoQueue = executeAsync(response, delayResponse, textType, mvAddress);
		} catch (Exception e) {
			logger.error("Could not process mv request", e);
		}
		logger.info("Is response sent is {}", isrequestAddedtoQueue);
		return isrequestAddedtoQueue;
	}

	private int checkConsumeInfo(jakarta.jms.Message message, StringBuilder messageData) throws JMSException {
		int textType = 0;
		if (message instanceof TextMessage || message instanceof ActiveMQTextMessage) {
			textType = 1;
			TextMessage textMessage = (TextMessage) message;
			messageData.append(textMessage.getText());
		} else if (message instanceof ActiveMQBytesMessage bytesMessage) {
			textType = 2;
			messageData.append(bytesMessage.getContent().data);
		}
		return textType;
	}

	/**
     * Sets up the JMS connection and session if they are not already initialized.
     */
	public void setup() {
		logger.info("Inside setup.");
		try {
			if (connection == null || ((ActiveMQConnection) connection).isClosed()) {
				logger.info("Creating new connection.");
				connection = activeMQConnectionFactory.createConnection();

				if (session == null) {
					logger.info("Starting new Session.");
					connection.start();
					this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				}
			}
		} catch (JMSException e) {
			logger.error(ExceptionUtils.getStackTrace(e), e);
		}
		logger.info("Setup Completed.");
	}

	/**
     * Consumes messages from the manual adjudication queue and processes them using the
     * {@link #consumeLogic(jakarta.jms.Message, String)} method.
     */
	public void runAdjudicationQueue() {
		try {
			QueueListener listener = new QueueListener() {

				@Override
				public void setListener(jakarta.jms.Message message) {
					consumeLogic(message, mvResponseAddress);

				}
			};
			consume(mvRequestAddress, listener, mabrokerUrl, mausername, mapassword);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	 /**
     * Consumes messages from the verification queue and processes them using the
     * {@link #consumeLogic(jakarta.jms.Message, String)} method.
     */
	public void runVerificationQueue() {
		try {
			QueueListener listener = new QueueListener() {

				@Override
				public void setListener(jakarta.jms.Message message) {
					consumeLogic(message, verificationResponseAddress);

				}
			};
			consume(verificationRequestAddress, listener, vbrokerUrl, vusername, vpassword);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	 /**
     * Consumes messages from the specified JMS queue address asynchronously.
     *
     * @param address   The JMS queue address to consume messages from.
     * @param object    The QueueListener object that handles the message consumption logic.
     * @param brokerUrl The URL of the broker for the JMS connection.
     * @param username  The username for authenticating the JMS connection.
     * @param password  The password for authenticating the JMS connection.
     * @return An empty byte array indicating successful consumption.
     * @throws MVException If an invalid connection configuration prevents connection creation.
     */
	@SuppressWarnings({ "unused" })
	public byte[] consume(String address, QueueListener object, String brokerUrl, String userName, String password) {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			logger.info("Creating new connection.");
			String failOverBrokerUrl = FAIL_OVER + brokerUrl + "," + brokerUrl + RANDOMIZE_FALSE;
			logger.info("Broker url : {}" , failOverBrokerUrl);
			this.activeMQConnectionFactory = new ActiveMQConnectionFactory(failOverBrokerUrl);
			this.activeMQConnectionFactory.setTrustedPackages(Arrays.asList("io.mosip.mock.mv.*"));
			this.activeMQConnectionFactory.setUserName(userName);
			this.activeMQConnectionFactory.setPassword(password);
		}
		
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			logger.error("Could not create connection. Invalid connection configuration.");
			throw new MVException(MVErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					MVErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}

		if (Objects.isNull(destination))
			setup();

		MessageConsumer messageConsumer;
		try {
			destination = session.createQueue(address);
			messageConsumer = session.createConsumer(destination);
			messageConsumer.setMessageListener(getListener(object));
		} catch (Exception e) {
			logger.error("consume", e);
		} 
		return new byte[0];
	}

	public static MessageListener getListener(QueueListener object) {
		return object::setListener;
	}

	/**
     * Creates and sends a byte array message to the specified JMS queue address.
     *
     * @param message The byte array message to send.
     * @param address The JMS queue address to send the message to.
     * @return true if the message was sent successfully; false otherwise.
     */
	public Boolean send(byte[] message, String address) {
		boolean flag = false;
		MessageProducer messageProducer = null;
		try {
			initialSetup();
			destination = session.createQueue(address);
			messageProducer = session.createProducer(destination);
			BytesMessage byteMessage = session.createBytesMessage();
			byteMessage.writeObject(message);
			messageProducer.send(byteMessage);
			flag = true;
		} catch (Exception e) {
			logger.error("send", e);
		} finally {
			try {
				if (!Objects.isNull(messageProducer))
					messageProducer.close();
			} catch (JMSException e) {
				logger.error("send", e);
			}
		}
		return flag;
	}

	 /**
     * Creates and sends a text message to the specified JMS queue address.
     *
     * @param message The text message to send.
     * @param address The JMS queue address to send the message to.
     * @return true if the message was sent successfully; false otherwise.
     */
	public Boolean send(String message, String address) {
		boolean flag = false;
		MessageProducer messageProducer = null;
		try {
			initialSetup();
			destination = session.createQueue(address);
			messageProducer = session.createProducer(destination);
			messageProducer.send(session.createTextMessage(message));

			flag = true;
		} catch (Exception e) {
			logger.error("send", e);
		} finally {
			try {
				if (!Objects.isNull(messageProducer))
					messageProducer.close();
			} catch (JMSException e) {
				logger.error("send", e);
			}
		}

		return flag;
	}

	/**
	 * Performs initial setup operations before executing the main functionality.
	 * This method checks the validity of the ActiveMQ connection factory and throws
	 * an MVException if the connection factory is null.
	 *
	 * @throws MVException if the ActiveMQ connection factory is null, indicating an
	 *         invalid connection scenario.
	 */
	private void initialSetup() throws MVException {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			logger.error("Inside initialSetup method. Invalid connection.");
			throw new MVException(MVErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					MVErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}
		setup();
	}

	/**
     * Returns a pre-configured ObjectMapper instance configured to ignore unknown properties.
     *
     * @return ObjectMapper instance with configured settings.
     */
	public static ObjectMapper objectMapper() {
		return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	 /**
     * Converts a Java object to its JSON representation as a String.
     *
     * @param className The object to convert to JSON.
     * @return JSON representation of the object as a String.
     * @throws JsonProcessingException If there's an error during JSON processing.
     */
	public static String javaObjectToJsonString(Object className) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		String outputJson = null;
		outputJson = objectMapper.writeValueAsString(className);
		return outputJson;
	}

	/**
     * Populates a list of candidates based on the provided reference IDs.
     *
     * @param refIds The list of reference IDs to create candidates from.
     * @return A CandidateList object populated with candidates.
     */
	private CandidateList populatesCandidateList(List<ReferenceIds> refIds) {
		List<Candidate> candidates = new ArrayList<>();
		CandidateList candidateList = new CandidateList();
		for (ReferenceIds refId : refIds) {
			Candidate candidate = new Candidate();
			candidate.setReferenceId(refId.getReferenceId());
			Map<String, String> analytics = new HashMap<>();
			AnalyticsDTO analyticsDTO = new AnalyticsDTO();
			analyticsDTO.setPrimaryOperatorID("110006");
			analyticsDTO.setPrimaryOperatorComments("abcd");
			analyticsDTO.setSecondaryOperatorComments("asbd");
			analyticsDTO.setSecondaryOperatorID("110005");
			analyticsDTO.setAnalytics(analytics);
			candidate.setAnalytics(analyticsDTO);
			candidates.add(candidate);
			candidateList.setCandidates(candidates);
		}
		return candidateList;
	}

	/**
     * Executes a task asynchronously after a specified delay, sending a response to a message broker.
     *
     * @param response      The response to send, either as a byte array or a String depending on textType.
     * @param delayResponse The delay in seconds before executing the task.
     * @param textType      The type of response: 1 for String, 2 for byte array.
     * @param mvAddress     The address of the message broker to send the response to.
     * @return true if the task was scheduled successfully, false otherwise.
     */
	public boolean executeAsync(String response, int delayResponse, Integer textType, String mvAddress) {
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					if (textType == 2) {
						send(response.getBytes(), mvAddress);
					} else if (textType == 1) {

						send(response, mvAddress);
					}
					logger.info("Scheduled job completed: MsgType {} ", textType);
				} catch (Exception e) {
					logger.error("executeAsync", e);
				}
			}
		};

		logger.info("Adding timed task with timer as {} seconds", delayResponse);
		timer.schedule(task, (long) delayResponse * 1000);
		return true;
	}
}