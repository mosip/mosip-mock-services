package io.mosip.proxy.abis.listener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.FailureResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.MockAbisQueueDetails;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.utility.Helpers;
import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

@Component
public class Listener {
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	@Value("${registration.processor.abis.json}")
	private String registrationProcessorAbisJson;

	@Value("${registration.processor.abis.response.delay:0}")
	private int delayResponse;

	/**
	 * Default UTC pattern.
	 */
	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private static final String ABIS_INSERT = "mosip.abis.insert";

	private static final String ABIS_IDENTIFY = "mosip.abis.identify";

	private static final String ABIS_DELETE = "mosip.abis.delete";

	private ActiveMQConnectionFactory activeMQConnectionFactory;

	/** The Constant INBOUNDQUEUENAME. */
	private static final String INBOUNDQUEUENAME = "inboundQueueName";

	/** The Constant OUTBOUNDQUEUENAME. */
	private static final String OUTBOUNDQUEUENAME = "outboundQueueName";

	/** The Constant ABIS. */
	private static final String ABIS = "abis";

	/** The Constant USERNAME. */
	private static final String USERNAME = "userName";

	/** The Constant PASSWORD. */
	private static final String PASSWORD = "password";

	/** The Constant BROKERURL. */
	private static final String BROKERURL = "brokerUrl";

	/** The Constant TYPEOFQUEUE. */
	private static final String TYPEOFQUEUE = "typeOfQueue";

	/** The Constant NAME. */
	private static final String NAME = "name";

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	private static final String TAG_ID = "id";
	private static final String TAG_VERSION = "version";
	private static final String TAG_REQUEST_ID = "requestId";
	private static final String TAG_REQUEST_TIME = "requesttime";
	private static final String TAG_REFERENCE_ID = "referenceId";
	private static final String TAG_REFERENCE_URL = "referenceURL";

	private Connection connection;
	private Session session;
	private Destination destination;

	/**
	 * This flag is added for development & debugging locally
	 * registration-processor-abis-sample.json If true then
	 * registration-processor-abis-sample.json will be picked from resources
	 */
	@Value("${local.development:false}")
	private boolean localDevelopment;

	private ProxyAbisController proxycontroller;

	private String outBoundQueue;

	@Autowired(required = true)
	public Listener(ProxyAbisController proxycontroller) {
		this.proxycontroller = proxycontroller;
	}

	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	public void consumeLogic(jakarta.jms.Message message, String abismiddlewareaddress)
			throws JMSException, InterruptedException {
		ResponseEntity<Object> obj = null;
		Map map = null;
		Integer textType = 0;
		String messageData = null;
		logger.info("Received message {}", message);
		try {
			if (message instanceof TextMessage || message instanceof ActiveMQTextMessage) {
				textType = 1;
				TextMessage textMessage = (TextMessage) message;
				messageData = textMessage.getText();
			} else if (message instanceof ActiveMQBytesMessage activeMQBytesMessage) {
				textType = 2;
				messageData = new String((activeMQBytesMessage).getContent().data);
			} else {
				logger.error("Received message is neither text nor byte");
				return;
			}
			logger.info("Message Data {}", messageData);
			map = new Gson().fromJson(messageData, Map.class);
			final ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

			logger.info("go on sleep {} ", delayResponse);
			TimeUnit.SECONDS.sleep(delayResponse);

			logger.info("Request type is {}", map.get("id"));

			switch (map.get(TAG_ID).toString()) {
			case ABIS_INSERT:
				final InsertRequestMO ie = mapper.convertValue(map, InsertRequestMO.class);
				proxycontroller.saveInsertRequestThroughListner(ie, textType);
				break;
			case ABIS_IDENTIFY:
				final IdentityRequest ir = mapper.convertValue(map, IdentityRequest.class);
				proxycontroller.identityRequestThroughListner(ir, textType);
				break;
			case ABIS_DELETE:
				final RequestMO mo = mapper.convertValue(map, RequestMO.class);
				proxycontroller.deleteRequestThroughListner(mo, textType);
				break;
			default:
				throw new AbisException(AbisErrorCode.INVALID_ID_EXCEPTION.getErrorCode(),
						AbisErrorCode.INVALID_ID_EXCEPTION.getErrorMessage());
			}
		} catch (AbisException e) {
			logger.error("Issue while hitting mock abis API", e);
			obj = errorRequestThroughListner(e, map, textType);
			try {
				proxycontroller.executeAsync(obj, delayResponse, textType);
			} catch (Exception e1) {
				logger.error("Issue while hitting mock abis API1", e);
			}
		}
	}

	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> errorRequestThroughListner(Exception ex, Map<String, String> map, int msgType) {
		logger.info("Error Request");
		String failureReason = "3";
		String id = "id";
		String requestId = TAG_REQUEST_ID;

		try {
			if (ex instanceof RequestException requestException) {
				failureReason = requestException.getReasonConstant();
			} else {
				failureReason = getFailureReason(map);
			}
			logger.info("failureReason {} ", failureReason);
			id = map.get("id");
			requestId = map.get(TAG_REQUEST_ID);

			FailureResponse fr = new FailureResponse(id, requestId, LocalDateTime.now(), "2", failureReason);
			return new ResponseEntity<>(fr, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("errorRequestThroughListner", e);
			FailureResponse fr = new FailureResponse(id, requestId, LocalDateTime.now(), "2", failureReason);
			return new ResponseEntity<>(fr, HttpStatus.OK);
		}
	}

	@SuppressWarnings({ "java:S1659", "java:S3776" })
	public String getFailureReason(Map<String, String> map) {
		String failureReason = "3";
		String id = "", versionInfo = "", requestId = "", requestTime = "", referenceId = "", referenceURL = "",
				gallery = "";
		id = map.get(TAG_ID);
		versionInfo = map.get(TAG_VERSION);
		requestId = map.get(TAG_REQUEST_ID);
		requestTime = map.get(TAG_REQUEST_TIME);
		referenceId = map.get(TAG_REFERENCE_ID);
		referenceURL = map.get(TAG_REFERENCE_URL);
		gallery = map.get("gallery");

		logger.info("id >>{}", id);
		logger.info("version >>{}", versionInfo);
		logger.info("requestId >>{}", requestId);
		logger.info("requestTime >>{}", requestTime);
		logger.info("referenceId >>{}", referenceId);
		logger.info("referenceURL >>{}", referenceURL);
		logger.info("gallery >>{}", gallery);

		if (Objects.isNull(id) || id.isBlank() || id.isEmpty() || !(id.equalsIgnoreCase(ABIS_INSERT)
				|| id.equalsIgnoreCase(ABIS_IDENTIFY) || id.equalsIgnoreCase(ABIS_DELETE))) {
			/*
			 * invalid id
			 */
			failureReason = FailureReasonsConstants.INVALID_ID;
			return failureReason;
		}

		if (Objects.isNull(versionInfo) || versionInfo.isBlank() || versionInfo.isEmpty()
				|| !(versionInfo.equalsIgnoreCase("1.1"))) {
			/*
			 * invalid version
			 */
			failureReason = FailureReasonsConstants.INVALID_VERSION;
			return failureReason;
		}

		if (Objects.isNull(requestId) || requestId.isBlank() || requestId.isEmpty()) {
			/*
			 * missing requestId (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REQUESTID;
			return failureReason;
		}

		if (Objects.isNull(requestTime) || requestTime.isBlank() || requestTime.isEmpty()) {
			/*
			 * missing requesttime (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REQUESTTIME;
			return failureReason;
		}
		if (!isValidFormat(UTC_DATETIME_PATTERN, requestTime, Locale.ENGLISH)) {
			/*
			 * invalid requesttime format
			 */
			failureReason = FailureReasonsConstants.INVALID_REQUESTTIME_FORMAT;
			return failureReason;
		}

		if (Objects.isNull(referenceId) || referenceId.isBlank() || referenceId.isEmpty()) {
			/*
			 * missing referenceId (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REFERENCEID;
			return failureReason;
		}

		if (id.equalsIgnoreCase(ABIS_INSERT)
				&& (referenceURL == null || referenceURL.isBlank() || referenceURL.isEmpty())) {
			/*
			 * missing reference URL (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REFERENCE_URL;
			return failureReason;
		}

		if (id.equalsIgnoreCase(ABIS_INSERT) && isValidInsertRequestDto(map)) {
			/*
			 * unable to serve the request - invalid request structure
			 */
			failureReason = FailureReasonsConstants.UNABLE_TO_SERVE_THE_REQUEST_INVALID_REQUEST_STRUCTURE;
			return failureReason;
		}
		if (id.equalsIgnoreCase(ABIS_IDENTIFY) && isValidIdentifyRequestDto(map)) {
			/*
			 * unable to serve the request - invalid request structure
			 */
			failureReason = FailureReasonsConstants.UNABLE_TO_SERVE_THE_REQUEST_INVALID_REQUEST_STRUCTURE;
			return failureReason;
		}

		return failureReason;
	}

	public static boolean isValidFormat(String format, String value, Locale locale) {
		LocalDateTime ldt = null;
		DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);

		try {
			ldt = LocalDateTime.parse(value, fomatter);
			String result = ldt.format(fomatter);
			return result.equals(value);
		} catch (DateTimeParseException e) {
			logger.error("isValidFormat", e);
			try {
				LocalDate ld = LocalDate.parse(value, fomatter);
				String result = ld.format(fomatter);
				return result.equals(value);
			} catch (DateTimeParseException exp) {
				logger.error("isValidFormat datetime", exp);
				try {
					LocalTime lt = LocalTime.parse(value, fomatter);
					String result = lt.format(fomatter);
					return result.equals(value);
				} catch (DateTimeParseException e2) {
					logger.error("isValidFormat", e2);
				}
			}
		}

		return false;
	}

	public static boolean isValidInsertRequestDto(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		boolean isOtherKeyPresent = false;

		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (!(entry.getKey().equals(TAG_ID) || entry.getKey().equals(TAG_VERSION)
					|| entry.getKey().equals(TAG_REQUEST_ID) || entry.getKey().equals(TAG_REQUEST_TIME)
					|| entry.getKey().equals(TAG_REFERENCE_ID) || entry.getKey().equals(TAG_REFERENCE_URL))) {
				isOtherKeyPresent = true;
				break;
			}
		}

		return isOtherKeyPresent;
	}

	public static boolean isValidIdentifyRequestDto(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		boolean isOtherKeyPresent = false;

		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (!(entry.getKey().equals(TAG_ID) || entry.getKey().equals(TAG_VERSION)
					|| entry.getKey().equals(TAG_REQUEST_ID) || entry.getKey().equals(TAG_REQUEST_TIME)
					|| entry.getKey().equals(TAG_REFERENCE_ID) || entry.getKey().equals(TAG_REFERENCE_URL)
					|| entry.getKey().equals("gallery"))) {
				isOtherKeyPresent = true;
				break;
			}
		}

		return isOtherKeyPresent;
	}

	public void sendToQueue(ResponseEntity<Object> obj, Integer textType) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		logger.info("Response: {}", obj.getBody());
		if (textType == 2) {
			send(mapper.writeValueAsString(obj.getBody()).getBytes(StandardCharsets.UTF_8), outBoundQueue);
		} else if (textType == 1) {
			send(mapper.writeValueAsString(obj.getBody()), outBoundQueue);
		}
	}

	public static String getJson(String configServerFileStorageURL, String uri, boolean localAbisQueueConf)
			throws IOException {
		if (localAbisQueueConf) {
			return Helpers.readFileFromResources("registration-processor-abis.json");
		} else {
			RestTemplate restTemplate = new RestTemplate();
			logger.info("Json URL {} {}", configServerFileStorageURL, uri);
			return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
		}
	}

	public List<MockAbisQueueDetails> getAbisQueueDetails() throws IOException {
		List<MockAbisQueueDetails> abisQueueDetailsList = new ArrayList<>();

		String registrationProcessorAbis = getJson(configServerFileStorageURL, registrationProcessorAbisJson,
				localDevelopment);

		logger.info("getAbisQueueDetails.....{}", registrationProcessorAbis);
		JSONObject regProcessorAbisJson;
		MockAbisQueueDetails abisQueueDetails = new MockAbisQueueDetails();
		Gson g = new Gson();

		try {
			regProcessorAbisJson = g.fromJson(registrationProcessorAbis, JSONObject.class);
			ArrayList<Map<String, String>> regProcessorAbisArray = (ArrayList<Map<String, String>>) regProcessorAbisJson
					.get(ABIS);

			for (int i = 0; i < regProcessorAbisArray.size(); i++) {
				Map<String, String> json = regProcessorAbisArray.get(i);
				String userName = validateAbisQueueJsonAndReturnValue(json, USERNAME);
				String password = validateAbisQueueJsonAndReturnValue(json, PASSWORD);
				String brokerUrl = validateAbisQueueJsonAndReturnValue(json, BROKERURL);
				String broker = brokerUrl.split("\\?")[0];
				String failOverBrokerUrl = FAIL_OVER + broker + "," + broker + RANDOMIZE_FALSE;
				String typeOfQueue = validateAbisQueueJsonAndReturnValue(json, TYPEOFQUEUE);
				String inboundQueueName = validateAbisQueueJsonAndReturnValue(json, INBOUNDQUEUENAME);
				String outboundQueueName = validateAbisQueueJsonAndReturnValue(json, OUTBOUNDQUEUENAME);
				String queueName = validateAbisQueueJsonAndReturnValue(json, NAME);

				this.activeMQConnectionFactory = new ActiveMQConnectionFactory(failOverBrokerUrl);
				this.activeMQConnectionFactory.setTrustedPackages(Arrays.asList("io.mosip.proxy.abis.*"));
				this.activeMQConnectionFactory.setUserName(userName);
				this.activeMQConnectionFactory.setPassword(password);

				abisQueueDetails.setTypeOfQueue(typeOfQueue);
				abisQueueDetails.setInboundQueueName(inboundQueueName);
				abisQueueDetails.setOutboundQueueName(outboundQueueName);
				abisQueueDetails.setName(queueName);
				abisQueueDetailsList.add(abisQueueDetails);
			}
		} catch (Exception e) {
			logger.error("Error while fetching abis info", e);
		}
		return abisQueueDetailsList;
	}

	private String validateAbisQueueJsonAndReturnValue(Map<String, String> jsonObject, String key)
			throws AbisException {
		String value = jsonObject.get(key);
		if (Objects.isNull(value)) {
			throw new AbisException(AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorCode(),
					AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorMessage() + key);
		}
		return value;
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
			logger.error(e.getMessage(), e);
		}
	}

	public void runAbisQueue() {
		try {
			List<MockAbisQueueDetails> abisQueueDetails = getAbisQueueDetails();
			if (abisQueueDetails != null && !abisQueueDetails.isEmpty()) {
				for (int i = 0; i < abisQueueDetails.size(); i++) {
					String outBoundAddress = abisQueueDetails.get(i).getOutboundQueueName();
					outBoundQueue = outBoundAddress;
					QueueListener listener = new QueueListener() {

						@Override
						public void setListener(jakarta.jms.Message message) {
							try {
								consumeLogic(message, outBoundAddress);
							} catch (JMSException | InterruptedException e) {
								logger.error("runAbisQueue", e);
								Thread.currentThread().interrupt();
							}
						}
					};
					consume(abisQueueDetails.get(i).getInboundQueueName(), listener,
							abisQueueDetails.get(i).getTypeOfQueue());
				}

			} else {
				throw new AbisException(AbisErrorCode.QUEUE_CONNECTION_NOT_FOUND_EXCEPTION.getErrorCode(),
						AbisErrorCode.QUEUE_CONNECTION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (Exception e) {
			logger.error("runAbisQueue", e);
		}
	}

	public byte[] consume(String address, QueueListener object, String queueName) throws AbisException {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			throw new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}
		if (destination == null) {
			setup();
		}
		MessageConsumer consumer = null;
		try {
			destination = session.createQueue(address);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(getListener(queueName, object));
		} catch (JMSException e) {
			logger.error("consume", e);
		} finally {
			if (!Objects.isNull(consumer))
				try {
					consumer.close();
				} catch (JMSException e) {
					logger.error("consume", e);
				}
		}
		throw new AbisException(AbisErrorCode.DATA_NULL_OR_EMPTY_EXCEPTION.getErrorCode(),
				AbisErrorCode.DATA_NULL_OR_EMPTY_EXCEPTION.getErrorMessage());
	}

	public static MessageListener getListener(String queueName, QueueListener object) {
		if (queueName.equals("ACTIVEMQ"))
			return object::setListener;

		return null;
	}

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
			if (!Objects.isNull(messageProducer))
				try {
					messageProducer.close();
				} catch (JMSException e) {
					logger.error("send", e);
				}
		}
		return flag;
	}

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
			if (!Objects.isNull(messageProducer))
				try {
					messageProducer.close();
				} catch (JMSException e) {
					logger.error("send", e);
				}
		}
		return flag;
	}

	private void initialSetup() throws AbisException {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			throw new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}
		setup();
	}
}