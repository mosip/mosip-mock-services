package io.mosip.proxy.abis.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.utility.Helpers;

@Configuration
public class JMSConfig {
	private static final Logger logger = LoggerFactory.getLogger(JMSConfig.class);

	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	@Value("${registration.processor.abis.json}")
	private String registrationProcessorAbisJson;

	@Value("${registration.processor.abis.response.delay:0}")
	private int delayResponse;

	/**
	 * This flag is added for development & debugging locally
	 * registration-processor-abis-sample.json If true then
	 * registration-processor-abis-sample.json will be picked from resources
	 */
	@Value("${local.development:false}")
	private boolean localDevelopment;

	/** The Constant FAIL_OVER. */
	private static final String FAIL_OVER = "failover:(";

	/** The Constant RANDOMIZE_FALSE. */
	private static final String RANDOMIZE_FALSE = ")?randomize=false";

	/** The Constant ABIS. */
	private static final String ABIS = "abis";

	/** The Constant USERNAME. */
	private static final String USERNAME = "userName";

	/** The Constant PASSWORD. */
	private static final String PASSWORD = "password";

	/** The Constant BROKERURL. */
	private static final String BROKERURL = "brokerUrl";

	@SuppressWarnings("unchecked")
	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		logger.info("Creating new connection from configuration.");
		Gson g = new Gson();
		ActiveMQConnectionFactory factory = null;
		try {
			String registrationProcessorAbis = getJson(configServerFileStorageURL, registrationProcessorAbisJson,
					localDevelopment);
			JSONObject regProcessorAbisJson = g.fromJson(registrationProcessorAbis, JSONObject.class);
			ArrayList<Map<String, String>> regProcessorAbisArray = (ArrayList<Map<String, String>>) regProcessorAbisJson.get(ABIS);
			for (int i = 0; i < regProcessorAbisArray.size(); i++) {
				Map<String, String> json = regProcessorAbisArray.get(i);
				String userName = validateAbisQueueJsonAndReturnValue(json, USERNAME);
				String password = validateAbisQueueJsonAndReturnValue(json, PASSWORD);
				String brokerUrl = validateAbisQueueJsonAndReturnValue(json, BROKERURL);
				String broker = brokerUrl.split("\\?")[0];
				String failOverBrokerUrl = FAIL_OVER + broker + "," + broker + RANDOMIZE_FALSE;

				factory = new ActiveMQConnectionFactory(failOverBrokerUrl);
				factory.setTrustedPackages(Arrays.asList("io.mosip.proxy.abis.*"));
				factory.setUserName(userName);
				factory.setPassword(password);
				return factory;
			}
		} catch (Exception e) {
			logger.error("Error while creating activeMQConnectionFactory info", e);
		}
		return factory;
	}

	private String getJson(String configServerFileStorageURL, String uri, boolean localAbisQueueConf)
			throws IOException {
		if (localAbisQueueConf) {
			return Helpers.readFileFromResources("registration-processor-abis.json");
		} else {
			RestTemplate restTemplate = new RestTemplate();
			logger.info("Json URL {} {}", configServerFileStorageURL, uri);
			return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
		}
	}

	private String validateAbisQueueJsonAndReturnValue(Map<String, String> jsonObject, String key)  {
		String value = jsonObject.get(key);
		if (Objects.isNull(value)) {
			throw new AbisException(AbisErrorCode.INVALID_KEY_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_KEY_EXCEPTION.getErrorMessage() + key);
		}
		return value;
	}
}