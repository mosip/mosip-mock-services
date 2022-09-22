package io.mosip.proxy.abis;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.MockAbisQueueDetails;
import io.mosip.proxy.abis.dto.RequestMO;

@Component
public class Listener {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	@Value("${registration.processor.abis.json}")
	private String registrationProcessorAbisJson;
	
	@Value("${registration.processor.abis.response.delay:0}")
	private int delayResponse;

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
	 * This flag is added for development & debugging locally registration-processor-abis-sample.json
	 * If true then registration-processor-abis-sample.json will be picked from resources
	 */
	@Value("${local.development:false}")
	private boolean localDevelopment;

	@Autowired
	ProxyAbisController proxycontroller;

	public String outBoundQueue;

	public void consumeLogic(javax.jms.Message message, String abismiddlewareaddress) {
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
				return ;
			}
			logger.info("Message Data " + messageData);
			Map map = new Gson().fromJson(messageData, Map.class);
			final ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

			ResponseEntity<Object> obj = null;

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
			}
		} catch (Exception e) {
			logger.error("Issue while hitting mock abis API", e.getMessage());
			e.printStackTrace();
		}
	}

	public void sendToQueue(ResponseEntity<Object> obj, Integer textType) throws JsonProcessingException, UnsupportedEncodingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		logger.info("Response: ", obj.getBody().toString());
		if (textType == 2) {
			send(mapper.writeValueAsString(obj.getBody()).getBytes("UTF-8"),
					outBoundQueue);
		} else if (textType == 1) {
			send(mapper.writeValueAsString(obj.getBody()), outBoundQueue);
		}
	}

	public static String getJson(String configServerFileStorageURL, String uri, boolean localAbisQueueConf) throws IOException, URISyntaxException {
		if (localAbisQueueConf) {
			return Helpers.readFileFromResources("registration-processor-abis.json");
		} else {
			RestTemplate restTemplate = new RestTemplate();
			logger.info("Json URL ",configServerFileStorageURL,uri);
			return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
		}
	}

	public List<MockAbisQueueDetails> getAbisQueueDetails() throws IOException, URISyntaxException {
		List<MockAbisQueueDetails> abisQueueDetailsList = new ArrayList<>();

		String registrationProcessorAbis = getJson(configServerFileStorageURL, registrationProcessorAbisJson, localDevelopment);
		
		logger.info(registrationProcessorAbis);
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
				String failOverBrokerUrl = FAIL_OVER + brokerUrl + "," + brokerUrl + RANDOMIZE_FALSE;
				String typeOfQueue = validateAbisQueueJsonAndReturnValue(json, TYPEOFQUEUE);
				String inboundQueueName = validateAbisQueueJsonAndReturnValue(json, INBOUNDQUEUENAME);
				String outboundQueueName = validateAbisQueueJsonAndReturnValue(json, OUTBOUNDQUEUENAME);
				String queueName = validateAbisQueueJsonAndReturnValue(json, NAME);

				this.activeMQConnectionFactory = new ActiveMQConnectionFactory(userName, password, failOverBrokerUrl);

				abisQueueDetails.setTypeOfQueue(typeOfQueue);
				abisQueueDetails.setInboundQueueName(inboundQueueName);
				abisQueueDetails.setOutboundQueueName(outboundQueueName);
				abisQueueDetails.setName(queueName);
				abisQueueDetailsList.add(abisQueueDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while fetching abis info", e.getMessage());
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
			logger.error(e.getMessage());
			e.printStackTrace();
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
						public void setListener(javax.jms.Message message) {
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
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	public byte[] consume(String address, QueueListener object, String queueName) throws Exception {

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
			consumer.setMessageListener(getListener(queueName, object));

		} catch (JMSException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
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
			
			//Message m = session.createMessage();
			//m.setJMSPriority(4);
			//m.setStringProperty("response", message);
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
		// this.activeMQConnectionFactory = new ActiveMQConnectionFactory(USERNAME,
		// PASSWORD, BROKERURL);
		if (this.activeMQConnectionFactory == null) {
			throw new Exception("Invalid Connection Exception");

		}
		setup();
	}

}
