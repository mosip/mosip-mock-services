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
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.DiscoverResponse;

import org.apache.commons.lang3.StringUtils;

public class DiscoverRequest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ObjectMapper oB = null;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// if(req.getMethod().contentEquals("MOSIPDISC"))
		if (req.getMethod().contentEquals("MOSIPDISC") || req.getMethod().contentEquals("GET")
				|| req.getMethod().contentEquals("POST"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("POST"))
			CORSManager.doOptions(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (oB == null)
			oB = new ObjectMapper();
		// add this line
		oB.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		oB.setVisibilityChecker(
				VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
		BufferedReader bR = request.getReader();
		String s = "";
		String sT = "";
		while ((s = bR.readLine()) != null) {
			sT = sT + s;
		}

		if(StringUtils.isBlank(sT))
		{
			sT = "{\"type\":\"Biometric Device\"}";
		}

		String[] splitString = sT.replace("{", "").replace("}", "").split(":");
		List<String> myList = Arrays.asList(splitString[1].split(","));
		List<DiscoverResponse> responseList = new ArrayList<DiscoverResponse>();
		myList.forEach(req -> {

			if (StringUtils.containsIgnoreCase(req, "Fingerprint")) {

				try {
					@SuppressWarnings("unchecked")
					DiscoverDto fingerDiscovery = oB.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DiscoverFIR.txt"))),
							DiscoverDto.class);

					responseList.add(buildDto(fingerDiscovery, "FIR"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (StringUtils.containsIgnoreCase(req, "Face")) {

				try {
					@SuppressWarnings("unchecked")
					DiscoverDto faceDiscovery = oB.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DiscoverFACE.txt"))),
							DiscoverDto.class);

					responseList.add(buildDto(faceDiscovery, "FACE"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (StringUtils.containsIgnoreCase(req, "Iris")) {

				try {
					@SuppressWarnings("unchecked")
					DiscoverDto irisDiscovery = oB.readValue(
							new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DiscoverIIR.txt"))),
							DiscoverDto.class);
					responseList.add(buildDto(irisDiscovery, "IIR"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (StringUtils.containsIgnoreCase(req, "Biometric Device")) {

				List<String> allModalityList = Arrays.asList("FIR", "IIR", "FACE");
				allModalityList.forEach(obj -> {

					DiscoverDto allDiscovery = null;
					try {
						allDiscovery = oB.readValue(
								new String(Files.readAllBytes(Paths.get(
										System.getProperty("user.dir") + "/files/MockMDS/Discover" + obj + ".txt"))),
								DiscoverDto.class);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					responseList.add(buildDto(allDiscovery, obj));

				});

			}

		});

		String info = new String(Files
				.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/" + "discoverInfo" + ".txt")));
		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();

		out.println(oB.writeValueAsString(responseList));
	}

	private DiscoverResponse buildDto(DiscoverDto fingerDiscovery, String modality) {

		DiscoverResponse discoverResponse = new DiscoverResponse();
		discoverResponse.callbackId = fingerDiscovery.callbackId;
		discoverResponse.certification = fingerDiscovery.certification;
		discoverResponse.deviceCode = fingerDiscovery.deviceCode;
		discoverResponse.deviceId = fingerDiscovery.deviceId;
		discoverResponse.deviceStatus = fingerDiscovery.deviceStatus;
		discoverResponse.deviceSubId = fingerDiscovery.deviceSubId;
		discoverResponse.digitalId = getDigitalFingerId(modality);
		discoverResponse.purpose = fingerDiscovery.purpose;
		discoverResponse.serviceVersion = fingerDiscovery.serviceVersion;
		discoverResponse.specVersion = fingerDiscovery.specVersion;
		discoverResponse.error = fingerDiscovery.error;
		return discoverResponse;
	}

	@SuppressWarnings("unchecked")
	public String getDigitalFingerId(String moralityType) {

		String digitalId = null;

		try {
			switch (moralityType) {

			case "FIR":
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalFingerId.txt"))),
						Map.class));

				break;
			case "IIR":
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalIrisId.txt"))),
						Map.class));

				break;
			case "FACE":
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalFaceId.txt"))),
						Map.class));

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return digitalId;
	}

	private String getDigitalModality(Map<String, String> digitalIdMap) {

		String result = null;
		Map<String, String> digitalMap = new LinkedHashMap<String, String>();
		digitalMap.put("dateTime", getTimeStamp());
		digitalMap.put("deviceProvider", digitalIdMap.get("deviceProvider"));
		digitalMap.put("deviceProviderId", digitalIdMap.get("deviceProviderId"));
		digitalMap.put("make", digitalIdMap.get("make"));
		digitalMap.put("serialNo", digitalIdMap.get("serialNo"));
		digitalMap.put("model", digitalIdMap.get("model"));
		digitalMap.put("subType", digitalIdMap.get("subType"));
		digitalMap.put("type", digitalIdMap.get("type"));
		try {
			result = Base64.getEncoder().encodeToString(oB.writeValueAsBytes(digitalMap));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;

	}

	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";

	}

}