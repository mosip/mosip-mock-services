package io.mosip.proxy.abis.listener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.FailureResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.MockAbisQueueDetails;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.utility.Helpers;

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

	@Autowired
	ProxyAbisController proxycontroller;

	public String outBoundQueue;

	public void consumeLogic(jakarta.jms.Message message, String abismiddlewareaddress) {
		ResponseEntity<Object> obj = null;
		Map map = null;
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
				return;
			}
			logger.info("Message Data " + messageData);
			map = new Gson().fromJson(messageData, Map.class);
			final ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

			logger.info("go on sleep {} ", delayResponse);
			TimeUnit.SECONDS.sleep(delayResponse);

			logger.info("Request type is " + map.get("id"));

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
				throw new Exception("Invalid id value");
			}
		} catch (Exception e) {
			logger.error("Issue while hitting mock abis API", e.getMessage());
			e.printStackTrace();
			obj = errorRequestThroughListner(e, map, textType);
			try {
				proxycontroller.executeAsync(obj, delayResponse, textType);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public ResponseEntity<Object> errorRequestThroughListner(Exception ex, Map<String, String> map, int msgType) {
		logger.info("Error Request");
		String failureReason = "3";
		String id = "id";
		String requestId = "requestId";

		try {
			if (ex instanceof RequestException) {
				failureReason = ((RequestException) ex).getReasonConstant();
			} else {
				failureReason = getFailureReason(map);
			}
			logger.info("failureReason >> " + failureReason);
			id = (String) map.get("id");
			requestId = (String) map.get("requestId");

			FailureResponse fr = new FailureResponse(id, requestId, LocalDateTime.now(), "2", failureReason);
			return new ResponseEntity<Object>(fr, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("errorRequestThroughListner", e);
			FailureResponse fr = new FailureResponse(id, requestId, LocalDateTime.now(), "2", failureReason);
			return new ResponseEntity<Object>(fr, HttpStatus.OK);
		}
	}

	public String getFailureReason(Map<String, String> map) {
		String failureReason = "3";
		String id = "", version = "", requestId = "", requestTime = "", referenceId = "", referenceURL = "",
				gallery = "";
		id = (String) map.get("id");
		version = (String) map.get("version");
		requestId = (String) map.get("requestId");
		requestTime = (String) map.get("requesttime");
		referenceId = (String) map.get("referenceId");
		referenceURL = (String) map.get("referenceURL");
		gallery = (String) map.get("gallery");

		logger.info("id >>" + id);
		logger.info("version >>" + version);
		logger.info("requestId >>" + requestId);
		logger.info("requestTime >>" + requestTime);
		logger.info("referenceId >>" + referenceId);
		logger.info("referenceURL >>" + referenceURL);
		logger.info("gallery >>" + gallery);

		if (id == null || id.isBlank() || id.isEmpty() || !(id.equalsIgnoreCase(ABIS_INSERT)
				|| id.equalsIgnoreCase(ABIS_IDENTIFY) || id.equalsIgnoreCase(ABIS_DELETE))) {
			/*
			 * invalid id
			 */
			failureReason = FailureReasonsConstants.INVALID_ID;
			return failureReason;
		}

		if (version == null || version.isBlank() || version.isEmpty() || !(version.equalsIgnoreCase("1.1"))) {
			/*
			 * invalid version
			 */
			failureReason = FailureReasonsConstants.INVALID_VERSION;
			return failureReason;
		}

		if (requestId == null || requestId.isBlank() || requestId.isEmpty()) {
			/*
			 * missing requestId (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REQUESTID;
			return failureReason;
		}

		if (requestTime == null || requestTime.isBlank() || requestTime.isEmpty()) {
			/*
			 * missing requesttime (in request body)
			 */
			failureReason = FailureReasonsConstants.MISSING_REQUESTTIME;
			return failureReason;
		}
		if (requestTime != null) {
			if (!isValidFormat(UTC_DATETIME_PATTERN, requestTime, Locale.ENGLISH)) {
				/*
				 * invalid requesttime format
				 */
				failureReason = FailureReasonsConstants.INVALID_REQUESTTIME_FORMAT;
				return failureReason;
			}
		}
		if (referenceId == null || referenceId.isBlank() || referenceId.isEmpty()) {
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
			e.printStackTrace();
			try {
				LocalDate ld = LocalDate.parse(value, fomatter);
				String result = ld.format(fomatter);
				return result.equals(value);
			} catch (DateTimeParseException exp) {
				exp.printStackTrace();
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
			if (!(entry.getKey().equals("id") || entry.getKey().equals("version") || entry.getKey().equals("requestId")
					|| entry.getKey().equals("requesttime") || entry.getKey().equals("referenceId")
					|| entry.getKey().equals("referenceURL"))) {
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
			if (!(entry.getKey().equals("id") || entry.getKey().equals("version") || entry.getKey().equals("requestId")
					|| entry.getKey().equals("requesttime") || entry.getKey().equals("referenceId")
					|| entry.getKey().equals("referenceURL") || entry.getKey().equals("gallery"))) {
				isOtherKeyPresent = true;
				break;
			}
		}

		return isOtherKeyPresent;
	}

	public void sendToQueue(ResponseEntity<Object> obj, Integer textType)
			throws JsonProcessingException, UnsupportedEncodingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		logger.info("Response: ", obj.getBody().toString());
		if (textType == 2) {
			send(mapper.writeValueAsString(obj.getBody()).getBytes("UTF-8"), outBoundQueue);
		} else if (textType == 1) {
			send(mapper.writeValueAsString(obj.getBody()), outBoundQueue);
		}
	}

	public static String getJson(String configServerFileStorageURL, String uri, boolean localAbisQueueConf)
			throws IOException, URISyntaxException {
		if (localAbisQueueConf) {
			return Helpers.readFileFromResources("registration-processor-abis.json");
		} else {
			RestTemplate restTemplate = new RestTemplate();
			logger.info("Json URL ", configServerFileStorageURL, uri);
			return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
		}
	}

	public List<MockAbisQueueDetails> getAbisQueueDetails() throws IOException, URISyntaxException {
		List<MockAbisQueueDetails> abisQueueDetailsList = new ArrayList<>();

		String registrationProcessorAbis = getJson(configServerFileStorageURL, registrationProcessorAbisJson,
				localDevelopment);

		logger.info("getAbisQueueDetails....." + registrationProcessorAbis);
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

	private String validateAbisQueueJsonAndReturnValue(Map<String, String> jsonObject, String key) throws Exception {

		String value = (String) jsonObject.get(key);
		if (value == null) {
			throw new Exception("Value does not exists for key" + key);
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
			if (abisQueueDetails != null && abisQueueDetails.size() > 0) {

				for (int i = 0; i < abisQueueDetails.size(); i++) {
					String outBoundAddress = abisQueueDetails.get(i).getOutboundQueueName();
					outBoundQueue = outBoundAddress;
					QueueListener listener = new QueueListener() {

						@Override
						public void setListener(jakarta.jms.Message message) {
							consumeLogic(message, outBoundAddress);
						}
					};
					consume(abisQueueDetails.get(i).getInboundQueueName(), listener,
							abisQueueDetails.get(i).getTypeOfQueue());
				}

			} else {
				throw new Exception("Queue Connection Not Found");

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public byte[] consume(String address, QueueListener object, String queueName) throws Exception {
		if (this.activeMQConnectionFactory == null) {
			throw new Exception("Invalid Connection Exception");
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
			logger.error(e.getMessage(), e);
		}
		return null;
	}

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
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return flag;
	}

	private void initialSetup() throws Exception {
		if (this.activeMQConnectionFactory == null) {
			throw new Exception("Invalid Connection Exception");

		}
		setup();
	}
}
