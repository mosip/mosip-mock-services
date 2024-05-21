package io.mosip.mock.mv.queue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import io.mosip.mock.mv.dto.AnalyticsDTO;
import io.mosip.mock.mv.dto.Candidate;
import io.mosip.mock.mv.dto.CandidateList;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.dto.ManualAdjudicationRequestDTO;
import io.mosip.mock.mv.dto.ManualAdjudicationResponseDTO;
import io.mosip.mock.mv.dto.ReferenceIds;
import io.mosip.mock.mv.service.ExpectationCache;
import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@Component
public class Listener {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	public static final String DECISION_SERVICE_ID = "mosip.registration.processor.manual.verification.decision.id";
	private static final String APPROVED = "APPROVED";
	private static final String REJECTED = "REJECTED";

	@Autowired
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

	public boolean consumeLogic(jakarta.jms.Message message, String mvAddress) {
		boolean isrequestAddedtoQueue = false;
		Integer textType = 0;
		String messageData = null;
		try {
			if (message instanceof TextMessage || message instanceof ActiveMQTextMessage) {
				textType = 1;
				TextMessage textMessage = (TextMessage) message;
				messageData = textMessage.getText();

			} else if (message instanceof ActiveMQBytesMessage) {
				textType = 2;
				messageData = new String(((ActiveMQBytesMessage) message).getContent().data);

			} else {
				logger.error("Received message is neither text nor byte");
				return false;
			}
			logger.info(String.format("Message Data %s", messageData));

			ManualAdjudicationRequestDTO requestDTO = objectMapper().readValue(messageData,
					ManualAdjudicationRequestDTO.class);
			ManualAdjudicationResponseDTO decisionDto = new ManualAdjudicationResponseDTO();
			decisionDto.setId(env.getProperty(DECISION_SERVICE_ID));
			decisionDto.setRequestId(requestDTO.getRequestId());
			decisionDto.setResponsetime(OffsetDateTime.now().toInstant().toString());
			decisionDto.setReturnValue(isSuccess ? 1 : 2);// logic needs to be implemented.
			int delayResponse = 0;
			if (mvAddress != verificationResponseAddress) {
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

			logger.info(String.format("Request type is %s", response));

			isrequestAddedtoQueue = executeAsync(response, delayResponse, textType, mvAddress);
		} catch (Exception e) {
			logger.error("Could not process mv request", ExceptionUtils.getStackTrace(e));
		}
		logger.info(String.format("Is response sent = %b", isrequestAddedtoQueue));
		return isrequestAddedtoQueue;
	}

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
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		logger.info("Setup Completed.");
	}

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
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

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
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public byte[] consume(String address, QueueListener object, String brokerUrl, String username, String password)
			throws Exception {

		if (activeMQConnectionFactory == null) {
			logger.info("Creating new connection.");
			String failOverBrokerUrl = FAIL_OVER + brokerUrl + "," + brokerUrl + RANDOMIZE_FALSE;
			logger.info(String.format("Broker url : %s", failOverBrokerUrl));
			this.activeMQConnectionFactory = new ActiveMQConnectionFactory(username, password, failOverBrokerUrl);
		}

		ActiveMQConnectionFactory activeMQConnectionFactory = this.activeMQConnectionFactory;
		if (activeMQConnectionFactory == null) {
			logger.error("Could not create connection. Invalid connection configuration.");
			throw new Exception("Invalid Connection Exception");

		}
		if (destination == null) {
			setup();
		}
		MessageConsumer consumer;
		try {
			destination = session.createQueue(address);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(getListener(object));

		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static MessageListener getListener(QueueListener object) {
		return new MessageListener() {
			@Override
			public void onMessage(Message message) {
				object.setListener(message);
			}
		};
	}

	public Boolean send(byte[] message, String address) {
		boolean flag = false;

		try {
			initialSetup();
			destination = session.createQueue(address);
			MessageProducer messageProducer = session.createProducer(destination);
			BytesMessage byteMessage = session.createBytesMessage();
			byteMessage.writeObject(message);
			messageProducer.send(byteMessage);
			flag = true;
		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return flag;
	}

	public Boolean send(String message, String address) {
		boolean flag = false;

		try {
			initialSetup();
			destination = session.createQueue(address);
			MessageProducer messageProducer = session.createProducer(destination);
			messageProducer.send(session.createTextMessage(message));

			flag = true;
		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return flag;
	}

	private void initialSetup() throws Exception {
		if (this.activeMQConnectionFactory == null) {
			logger.error("Inside initialSetup method. Invalid connection.");
			throw new Exception("Invalid Connection Exception");
		}
		setup();
	}

	public static ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		return objectMapper;
	}

	public static String javaObjectToJsonString(Object className) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		String outputJson = null;
		outputJson = objectMapper.writeValueAsString(className);
		return outputJson;
	}

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

	public boolean executeAsync(String response, int delayResponse, Integer textType, String mvAddress) {
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					if (textType == 2) {
						send(response.getBytes(), mvAddress);
					} else if (textType == 1) {

						send(response, mvAddress);
					}
					logger.info(String.format("Scheduled job completed: MsgType %d ", textType));
				} catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		};

		logger.info(String.format("Adding timed task with timer as %d seconds", delayResponse));
		timer.schedule(task, delayResponse * 1000);
		return true;
	}

}
