package io.mosip.mock.mv.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import io.mosip.mock.mv.dto.AnalyticsDTO;
import io.mosip.mock.mv.dto.Candidate;
import io.mosip.mock.mv.dto.CandidateList;
import io.mosip.mock.mv.dto.ManualAdjudicationRequestDTO;
import io.mosip.mock.mv.dto.ManualAdjudicationResponseDTO;
import io.mosip.mock.mv.dto.ManualVerificationDecisionDto;
import io.mosip.mock.mv.dto.ManualVerificationStatus;
import io.mosip.mock.mv.dto.ReferenceIds;
import io.mosip.mock.mv.dto.ResponseWrapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Listener {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	public static final String DECISION_SERVICE_ID = "mosip.registration.processor.manual.verification.decision.id";

	@Autowired
	private Environment env;

	/** The username. */
	@Value("${mock.mv.decision:}")
	private String mockDecision;

	/** The username. */
	@Value("${registration.processor.queue.username}")
	private String username;

	/** The password. */
	@Value("${registration.processor.queue.password}")
	private String password;

	@Value("${registration.processor.queue.url}")
	private String brokerUrl;

	/** The type of queue. */
	@Value("${registration.processor.queue.typeOfQueue}")
	private String typeOfQueue;

	/** The address. */
	@Value("${registration.processor.queue.manualverification.response:mv-to-mosip}")
	private String mvResponseAddress;

	/** The address. */
	@Value("${registration.processor.queue.manualverification.request:mosip-to-mv}")
	private String mvRequestAddress;


	private ActiveMQConnectionFactory activeMQConnectionFactory;

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	private Connection connection;
	private Session session;
	private Destination destination;

	public boolean consumeLogic(javax.jms.Message message, String mvAddress) {
		boolean isrequestAddedtoQueue = false;
		Integer textType = 0;
		String messageData = null;
		logger.info("Received message " + message);
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
			logger.info("Message Data " + messageData);

			ManualAdjudicationRequestDTO requestDTO = objectMapper().readValue(messageData, ManualAdjudicationRequestDTO.class);

			ManualAdjudicationResponseDTO decisionDto = new ManualAdjudicationResponseDTO();
			decisionDto.setId(env.getProperty(DECISION_SERVICE_ID));
			decisionDto.setRequestId(requestDTO.getRequestId());
			decisionDto.setResponsetime(LocalDateTime.now());
			decisionDto.setReturnValue(requestDTO.getGallery().getReferenceIds().size());// logic needs to be implemented.
			
			List<ReferenceIds> refIds=requestDTO.getGallery().getReferenceIds();
			List<Candidate> candidates=new ArrayList<>();
			for(ReferenceIds refId:refIds) {// logic needs to be implemented.
				Candidate candidate=new Candidate();
				candidate.setReferenceId(refId.getReferenceId());
				Map<String,String> analytics=new HashMap<>();
				AnalyticsDTO analyticsDTO=new AnalyticsDTO();
				analyticsDTO.setPrimaryOperatorID("110006");
				analyticsDTO.setPrimaryOperatorComments("abcd");
				analyticsDTO.setSecondaryOperatorComments("asbd");
				analyticsDTO.setSecondaryOperatorID("110005");
				analyticsDTO.setAnalytics(analytics);
				candidate.setAnalytics(analyticsDTO);
				candidates.add(candidate);
			}
			CandidateList candidateList=new CandidateList();
			candidateList.setCandidates(candidates);
			candidateList.setCount(requestDTO.getGallery().getReferenceIds().size());// logic needs to be implemented.
			Map<String,String> analytics=new HashMap<>();
			analytics.put("primaryOperatorID", "110006");//logic needs to be implemented
			analytics.put("primaryOperatorComments", "abcd");
			candidateList.setAnalytics(analytics);
			decisionDto.setCandidateList(candidateList);
			String response = javaObjectToJsonString(decisionDto);
			

			logger.info("Request type is " + response);

			if (textType == 2) {
				isrequestAddedtoQueue = send(response.getBytes(), mvAddress);
			} else if (textType == 1) {
				isrequestAddedtoQueue = send(response, mvAddress);
			}
		} catch (Exception e) {
			logger.error("Could not process mv request", ExceptionUtils.getStackTrace(e));
		}
		logger.info("Is response sent = " + isrequestAddedtoQueue);
		return isrequestAddedtoQueue;
	}

	public void setup() {
		try {
			if (connection == null || ((ActiveMQConnection) connection).isClosed()) {
				connection = activeMQConnectionFactory.createConnection();

				if (session == null) {
					connection.start();
					this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				}
			}
		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public void runMvQueue() {
		try {
			QueueListener listener = new QueueListener() {

				@Override
				public void setListener(javax.jms.Message message) {
					consumeLogic(message, mvResponseAddress);

				}
			};
			consume(mvRequestAddress, listener);

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public byte[] consume(String address, QueueListener object) throws Exception {

		if (activeMQConnectionFactory == null) {
			String failOverBrokerUrl = FAIL_OVER + brokerUrl + "," + brokerUrl + RANDOMIZE_FALSE;
			this.activeMQConnectionFactory = new ActiveMQConnectionFactory(username, password, failOverBrokerUrl);
		}

		ActiveMQConnectionFactory activeMQConnectionFactory = this.activeMQConnectionFactory;
		if (activeMQConnectionFactory == null) {

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
			throw new Exception("Invalid Connection Exception");
		}
		setup();
	}

	public static ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

}
