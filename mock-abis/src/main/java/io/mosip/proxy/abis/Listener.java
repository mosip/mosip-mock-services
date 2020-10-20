package io.mosip.proxy.abis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.InsertRequestMO;
import io.mosip.proxy.abis.entity.RequestMO;

@Component
public class Listener {
	
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	

	@Autowired
	ProxyAbisController proxycontroller;

	@JmsListener(destination = "inbound.queue")
	@SendTo("outbound.queue")
	public String receiveMessage(final Message jsonMessage) throws JMSException, Exception {
		String messageData = null;
		logger.info("Received message " + jsonMessage);

		if (!(jsonMessage instanceof TextMessage)) {
			return null;
		}

		TextMessage textMessage = (TextMessage) jsonMessage;
		messageData = textMessage.getText();
		logger.info("Message Data " + messageData);
		Map map = new Gson().fromJson(messageData, Map.class);
		final ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
	    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		
		ResponseEntity<Object> obj = null;

		logger.info("Request type is " + map.get("id"));

		switch (map.get("id").toString()) {

		case "mosip.abis.insert":
			final InsertRequestMO ie = mapper.convertValue(map, InsertRequestMO.class);
			obj = proxycontroller.saveInsertRequestThroughListner(ie);
			break;
		case "mosip.abis.identify":
			final IdentityRequest ir = mapper.convertValue(map, IdentityRequest.class);
			obj = proxycontroller.identityRequestThroughListner(ir);
			break;
		case "mosip.abis.delete":
			final RequestMO mo = mapper.convertValue(map, RequestMO.class);
			obj = proxycontroller.deleteRequestThroughListner(mo);
			break;
		}

		logger.info("Response " + mapper.writeValueAsString(obj.getBody()));
		return mapper.writeValueAsString(obj.getBody());

	}
}
