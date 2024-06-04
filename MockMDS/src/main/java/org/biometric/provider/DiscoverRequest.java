package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.DiscoverResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("deprecation")
public class DiscoverRequest extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(DiscoverRequest.class);
	/** User Dir. */
	public static final String USER_DIR = "user.dir";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ObjectMapper objMapper = null;

	static {
		objMapper = new ObjectMapper();		
		// add this line
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objMapper.setVisibilityChecker(
				VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
	}
	
	public DiscoverRequest() {
		super();
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("MOSIPDISC"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("POST"))
			CORSManager.doOptions(req, res);
	}

	@Override
	@SuppressWarnings({ "java:S1989", "java:S3776", "unchecked", "deprecation" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader bR = request.getReader();
		String s = "";
		StringBuilder sT = new StringBuilder();
		while ((s = bR.readLine()) != null) {
			sT.append(s);
		}

		if (StringUtils.isBlank(sT)) {
			sT.append("{\"type\":\"Biometric Device\"}");
		}

		String[] splitString = sT.toString().replace("{", "").replace("}", "").split(":");
		List<String> myList = Arrays.asList(splitString[1].split(","));
		List<DiscoverResponse> responseList = new ArrayList<>();
		myList.forEach(req -> {

			if (StringUtils.containsIgnoreCase(req, "Fingerprint")) {
				try {
					DiscoverDto fingerDiscovery = objMapper.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DiscoverFIR.txt"))),
							DiscoverDto.class);

					responseList.add(buildDto(fingerDiscovery, "FIR"));
				} catch (IOException e) {
					logger.error("doPost fingerprint", e);
				}
			} else if (StringUtils.containsIgnoreCase(req, "Face")) {
				try {
					DiscoverDto faceDiscovery = objMapper.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DiscoverFACE.txt"))),
							DiscoverDto.class);

					responseList.add(buildDto(faceDiscovery, "FACE"));
				} catch (IOException e) {
					logger.error("doPost face", e);
				}
			} else if (StringUtils.containsIgnoreCase(req, "Iris")) {
				try {
					DiscoverDto irisDiscovery = objMapper.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DiscoverIIR.txt"))),
							DiscoverDto.class);
					responseList.add(buildDto(irisDiscovery, "IIR"));
				} catch (IOException e) {
					logger.error("doPost iris", e);
				}
			} else if (StringUtils.containsIgnoreCase(req, "Biometric Device")) {
				List<String> allModalityList = Arrays.asList("FIR", "IIR", "FACE");
				allModalityList.forEach(obj -> {

					DiscoverDto allDiscovery = null;
					try {
						allDiscovery = objMapper.readValue(
								new String(Files.readAllBytes(Paths
										.get(System.getProperty(USER_DIR) + "/files/MockMDS/Discover" + obj + ".txt"))),
								DiscoverDto.class);
					} catch (IOException e) {
						logger.error("doPost all", e);
					}
					responseList.add(buildDto(allDiscovery, obj));
				});
			}
		});

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();

		out.println(objMapper.writeValueAsString(responseList));
	}

	private DiscoverResponse buildDto(DiscoverDto fingerDiscovery, String modality) {
		DiscoverResponse discoverResponse = new DiscoverResponse();
		discoverResponse.setCallbackId(fingerDiscovery.getCallbackId());
		discoverResponse.setCertification(fingerDiscovery.getCertification());
		discoverResponse.setDeviceCode(fingerDiscovery.getDeviceCode());
		discoverResponse.setDeviceId(fingerDiscovery.getDeviceId());
		discoverResponse.setDeviceStatus(fingerDiscovery.getDeviceStatus());
		discoverResponse.setDeviceSubId(fingerDiscovery.getDeviceSubId());
		discoverResponse.setDigitalId(getDigitalId(modality));
		discoverResponse.setPurpose(fingerDiscovery.getPurpose());
		discoverResponse.setServiceVersion(fingerDiscovery.getServiceVersion());
		discoverResponse.setSpecVersion(fingerDiscovery.getSpecVersion());
		discoverResponse.setError(fingerDiscovery.getError());
		return discoverResponse;
	}

	@SuppressWarnings("unchecked")
	public String getDigitalId(String modalityType) {
		String digitalId = null;

		try {
			switch (modalityType) {
			case "FIR":
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalFingerId.txt"))),
						Map.class));
				break;
				
			case "IIR":
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalIrisId.txt"))),
						Map.class));
				break;
				
			case "FACE":
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalFaceId.txt"))),
						Map.class));
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("getDigitalId", e);
		}

		return digitalId;
	}

	private String getDigitalModality(Map<String, String> digitalIdMap) throws JsonProcessingException {
		Map<String, String> digitalMap = new LinkedHashMap<>();
		digitalMap.put("dateTime", getTimeStamp());
		digitalMap.put("deviceProvider", digitalIdMap.get("deviceProvider"));
		digitalMap.put("deviceProviderId", digitalIdMap.get("deviceProviderId"));
		digitalMap.put("make", digitalIdMap.get("make"));
		digitalMap.put("serialNo", digitalIdMap.get("serialNo"));
		digitalMap.put("model", digitalIdMap.get("model"));
		digitalMap.put("deviceSubType", digitalIdMap.get("deviceSubType"));
		digitalMap.put("type", digitalIdMap.get("type"));
		return io.mosip.mock.sbi.util.StringHelper.base64UrlEncode(objMapper.writeValueAsBytes(digitalMap));
	}

	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";
	}
}