package io.mosip.proxy.abis.listener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import jakarta.jms.Message;
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

	private static final String ID = "id";
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

	private static final String VALUE = "value";

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

	public String outBoundQueue;

	/**
	 * Constructor for the Listener class.
	 *
	 * @param proxycontroller The ProxyAbisController instance.
	 */
	@Autowired(required = true)
	public Listener(ProxyAbisController proxycontroller) {
		this.proxycontroller = proxycontroller;
	}
	
	/**
	 * Consumes and processes the received JMS message.
	 *
	 * @param message               The received JMS message.
	 * @param abismiddlewareaddress The address of the ABIS middleware.
	 * @throws JMSException         If there is an issue with JMS operations.
	 * @throws InterruptedException If the thread is interrupted.
	 */
	public void consumeLogic(jakarta.jms.Message message, String abismiddlewareaddress) throws JMSException, InterruptedException {
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
				messageData = new String(activeMQBytesMessage.getContent().data);
			} else {
				logger.error("Received message is neither text nor byte");
				return;
			}
			logger.info("Message Data {} ", messageData);
			map = new Gson().fromJson(messageData, Map.class);
			final ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			
			logger.info("go on sleep {} ", delayResponse);
			TimeUnit.SECONDS.sleep(delayResponse);

			logger.info("Request type is {} ", map.get("id"));

			switch (map.get(ID).toString()) {
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
		} catch (Exception e) {
			logger.error("Issue while hitting mock abis API", e);
			obj = errorRequestThroughListner(e, map, textType);
			try {
				proxycontroller.executeAsync(obj, delayResponse, textType);
			} catch (Exception e1) {
				logger.error("Issue while hitting mock abis API1", e1);
			}
		}
	}

	/**
	 * Handles error scenarios for the listener.
	 *
	 * @param ex      The exception that occurred.
	 * @param map     The map containing request details.
	 * @param msgType The type of message (text or bytes).
	 * @return The ResponseEntity object containing the failure response.
	 */
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

	/**
	 * Retrieves the failure reason from the provided map.
	 *
	 * @param map The map containing request details.
	 * @return The failure reason.
	 */
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

	/**
	 * Validates if a string value conforms to a specific date/time format based on
	 * the provided locale. This method offers a robust approach for validating
	 * various date and time representations including full date-time, date only, or
	 * time only.
	 *
	 * @param format The date/time format pattern in compliance with Java's
	 *               DateTimeFormatter syntax.
	 * @param value  The string value to be validated against the specified format.
	 * @param locale The Locale object representing the cultural context for parsing
	 *               (e.g., delimiters, separators).
	 * @return true if the value adheres to the provided format within the given
	 *         locale, false otherwise.
	 * @throws IllegalArgumentException if the format pattern is invalid.
	 */
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

	/**
	 * Validates the structure of a map representing an insert request data object.
	 * This method ensures that the map contains only allowed keys relevant to
	 * insert requests, rejecting any maps containing unauthorized keys.
	 *
	 * @param requestData The map containing insert request data (key-value pairs).
	 * @return true if the map contains any unauthorized keys, false if the
	 *         structure is valid (all keys are allowed).
	 * @throws NullPointerException if the provided requestData map is null.
	 */
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

	/**
	 * Validates the structure of a map representing an identify request data
	 * object. This method ensures that the map contains only allowed keys relevant
	 * to identify requests, including the standard keys for insert requests
	 * (`TAG_ID`, `TAG_VERSION`, etc.) and an additional key named "gallery"
	 * (specific purpose to be clarified).
	 *
	 * @param requestData The map containing identify request data (key-value
	 *                    pairs).
	 * @return true if the map contains any unauthorized keys, false if the
	 *         structure is valid (all keys are allowed).
	 * @throws NullPointerException if the provided requestData map is null.
	 */
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

	/**
	 * Serializes a provided response object (ResponseEntity) into JSON format and
	 * sends it to the outbound queue. The method employs different encoding
	 * strategies based on the specified text type.
	 *
	 * @param obj      The ResponseEntity object containing the response data.
	 * @param textType An integer value indicating the text type (specific
	 *                 interpretation required).
	 * @throws JsonProcessingException if an error occurs during JSON serialization.
	 */
	public void sendToQueue(ResponseEntity<Object> obj, Integer textType)
			throws JsonProcessingException, UnsupportedEncodingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		logger.info("Response: {} ", obj.getBody());
		if (textType == 2) {
			send(mapper.writeValueAsString(obj.getBody()).getBytes(StandardCharsets.UTF_8), outBoundQueue);
		} else if (textType == 1) {
			send(mapper.writeValueAsString(obj.getBody()), outBoundQueue);
		}
	}

	/**
	 * Retrieves the JSON configuration for ABIS queues based on the provided
	 * configuration details. This method offers a flexible approach for obtaining
	 * the configuration: - If the `localAbisQueueConf` flag is true, it assumes a
	 * local JSON file named "registration-processor-abis.json" exists within the
	 * classpath and reads its contents using a helper method (assumed to exist). -
	 * If `localAbisQueueConf` is false, it fetches the configuration from a remote
	 * server using a RestTemplate instance. The URL is constructed by concatenating
	 * the `configServerFileStorageURL` and `uri` parameters.
	 *
	 * @param configServerFileStorageURL The base URL for the configuration server
	 *                                   (if remote fetching is used).
	 * @param uri                        The specific URI path for the ABIS queue
	 *                                   configuration on the server.
	 * @param localAbisQueueConf         A boolean flag indicating whether to use
	 *                                   local ("true") or remote ("false")
	 *                                   configuration.
	 * @return The retrieved JSON configuration as a String.
	 * @throws IOException if an error occurs during file reading or remote server
	 *                     communication.
	 */
	public static String getJson(String configServerFileStorageURL, String uri, boolean localAbisQueueConf)
			throws IOException {
		if (localAbisQueueConf) {
			return Helpers.readFileFromResources("registration-processor-abis.json");
		} else {
			RestTemplate restTemplate = new RestTemplate();
			logger.info("Json URL {} {} ", configServerFileStorageURL, uri);
			return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
		}
	}

	/**
	 * Retrieves and parses the ABIS queue configuration details. This method
	 * fetches the JSON configuration (using `getJson`) and parses it to populate a
	 * list of `MockAbisQueueDetails` objects.
	 *
	 * @return A list of `MockAbisQueueDetails` objects containing parsed queue
	 *         information.
	 * @throws IOException if an error occurs during file reading or JSON parsing.
	 */
	public List<MockAbisQueueDetails> getAbisQueueDetails() throws IOException, URISyntaxException {
		List<MockAbisQueueDetails> abisQueueDetailsList = new ArrayList<>();

		String registrationProcessorAbis = getJson(configServerFileStorageURL, registrationProcessorAbisJson,
				localDevelopment);

		logger.info("getAbisQueueDetails.....{}", registrationProcessorAbis);
		JSONObject regProcessorAbisJson;
		MockAbisQueueDetails abisQueueDetails = new MockAbisQueueDetails();
		Gson g = new Gson();

		try {
			regProcessorAbisJson = g.fromJson(registrationProcessorAbis, JSONObject.class);

			ArrayList<Map> regProcessorAbisArray = (ArrayList<Map>) regProcessorAbisJson.get(ABIS);

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

	/**
	 * Validates if a specific key exists within a JSON object representing ABIS
	 * queue configuration. If the key is missing, an `AbisException` is thrown with
	 * a specific error code and message indicating the missing key. Otherwise, the
	 * value associated with the key is returned.
	 *
	 * @param jsonObject The JSON object containing ABIS queue configuration data
	 *                   (key-value pairs).
	 * @param key        The key to search for within the JSON object.
	 * @return The value associated with the key if found, throws AbisException if
	 *         the key is missing.
	 * @throws AbisException if the specified key is not found in the JSON object.
	 */
	private String validateAbisQueueJsonAndReturnValue(Map<String, String> jsonObject, String key)
			throws AbisException {
		String value = jsonObject.get(key);
		if (Objects.isNull(value)) {
			throw new AbisException(AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorCode(),
					AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorMessage() + key);
		}
		return value;
	}

	/**
	 * Establishes a connection to the ActiveMQ server and creates a session if
	 * necessary. This method attempts to re-establish a connection if it's closed
	 * or null. A session is created only if the connection is successfully
	 * established and currently doesn't have an active session.
	 *
	 * @throws JMSException if an error occurs during connection or session
	 *                      creation.
	 */
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

	/**
	 * Starts the processing of messages from the configured ABIS queues. This
	 * method retrieves the ABIS queue details using `getAbisQueueDetails`. If
	 * details are found, it iterates through each queue, sets the outbound queue
	 * name, and creates a message listener for the inbound queue. The listener
	 * calls the `consumeLogic` method to process the received message. If no queue
	 * details are found, an `AbisException` is thrown indicating missing
	 * configuration.
	 *
	 * @throws AbisException if an error occurs during configuration retrieval,
	 *                       message consumption, or if no queue details are found.
	 */
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
				throw new Exception("Queue Connection Not Found");

			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("An unexpected error occurred.", e); // Logs stack trace securely if configured
			// Optionally rethrow or handle the exception gracefully
		}

	}

	/**
	 * Consumes messages from a specified JMS queue using a provided message
	 * listener. This method validates the connection factory and establishes a
	 * connection/session if necessary using `setup`. It creates a destination
	 * (queue object) and a message consumer for the provided address. The consumer
	 * is then assigned the message listener based on the queue name and provided
	 * object. The method closes the consumer in a finally block.
	 *
	 * @param address   The JMS queue address (name) to consume messages from.
	 * @param object    The `QueueListener` object that will handle received
	 *                  messages.
	 * @param queueName The name of the queue (used for listener assignment -
	 *                  purpose to be clarified).
	 * @return An empty byte array (consider modifying the return type if needed).
	 * @throws AbisException if the connection factory is invalid, an error occurs
	 *                       during connection/session setup, or a JMS exception
	 *                       occurs.
	 */
	@SuppressWarnings({ "java:S2095" })
	public byte[] consume(String address, QueueListener object, String queueName) throws AbisException {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			throw new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}
		if (destination == null) {
			setup();
		}
		MessageConsumer consumer;
		try {
			destination = session.createQueue(address);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(getListener(queueName, object));
		} catch (JMSException e) {
			logger.error("consume", e);
		}
		return new byte[0];
	}

	/**
	 * Assigns a message listener based on the provided queue name. This method
	 * currently supports only a single listener type ("ACTIVEMQ") that delegates
	 * the message handling to the provided `QueueListener` object's `setListener`
	 * method. The purpose of using the queue name for listener selection needs
	 * further clarification. If different listener types are required, this method
	 * can be extended to handle them.
	 *
	 * @param queueName The name of the JMS queue.
	 * @param object    The `QueueListener` object to be used for message handling.
	 * @return A `MessageListener` instance based on the queue name, or null if no
	 *         matching listener type is found.
	 */
	public static MessageListener getListener(String queueName, QueueListener object) {
		if (queueName.equals("ACTIVEMQ")) {

			return new MessageListener() {
				@Override
				public void onMessage(Message message) {
					object.setListener(message);
				}
			};

		}
		return null;
	}

	/**
	 * Sends a byte array message to the specified JMS queue address. This method
	 * establishes a connection/session if necessary using `initialSetup`. It
	 * creates a destination (queue object), a message producer, and a
	 * `BytesMessage` object. The message is written as an object to the
	 * `BytesMessage`, and then sent using the message producer. The method closes
	 * the message producer in a finally block.
	 *
	 * @param message The byte array representing the message to be sent.
	 * @param address The JMS queue address (name) to send the message to.
	 * @return True if the message was sent successfully, false otherwise.
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
			if (!Objects.isNull(messageProducer))
				try {
					messageProducer.close();
				} catch (JMSException e) {
					logger.error("send", e);
				}
		}
		return flag;
	}

	/**
	 * Sends a String message to the specified JMS queue address. This method
	 * establishes a connection/session if necessary using `initialSetup`. It
	 * creates a destination (queue object), a message producer, and a
	 * `BytesMessage` object. The message is written as an object to the
	 * `BytesMessage`, and then sent using the message producer. The method closes
	 * the message producer in a finally block.
	 *
	 * @param message The String representing the message to be sent.
	 * @param address The JMS queue address (name) to send the message to.
	 * @return True if the message was sent successfully, false otherwise.
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
			if (!Objects.isNull(messageProducer))
				try {
					messageProducer.close();
				} catch (JMSException e) {
					logger.error("send", e);
				}
		}
		return flag;
	}


	/**
	 * Initializes a connection and session to the ActiveMQ server if they are not
	 * already established. This method delegates the setup logic to the `setup`
	 * method.
	 *
	 * @throws AbisException if the connection factory is invalid.
	 */
	private void initialSetup() throws AbisException {
		if (Objects.isNull(this.activeMQConnectionFactory)) {
			throw new AbisException(AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
		}
		setup();
	}
}