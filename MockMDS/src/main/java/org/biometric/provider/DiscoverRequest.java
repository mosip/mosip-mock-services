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

	/**
	 * Default constructor for the {@code DiscoverRequest} class.
	 *
	 * <p>This constructor calls the superclass constructor to initialize the servlet.</p>
	 */
	public DiscoverRequest() {
		super();
	}

	/**
	 * Handles HTTP requests by delegating to the appropriate method based on the request type.
	 *
	 * <p>This method overrides the {@code service} method of {@code HttpServlet} to handle
	 * custom HTTP methods. It delegates processing to the {@code doPost} method for the "MOSIPDISC"
	 * method, and to the {@code CORSManager.doOptions} method for "OPTIONS" and "POST" methods to
	 * manage Cross-Origin Resource Sharing (CORS) preflight requests.</p>
	 *
	 * @param req  the {@code HttpServletRequest} object that contains the request the client made to the servlet
	 * @param res  the {@code HttpServletResponse} object that contains the response the servlet returns to the client
	 * @throws ServletException if the request could not be handled
	 * @throws IOException if an input or output error occurs while the servlet is handling the request
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("MOSIPDISC"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("POST"))
			CORSManager.doOptions(req, res);
	}

	/**
	 * Handles POST requests for the {@code DiscoverRequest} servlet.
	 *
	 * <p>This method processes the POST request to discover biometric devices. It reads the request body,
	 * parses the specified modalities, and constructs the appropriate {@link DiscoverResponse} objects
	 * based on the discovered device information. The response is then written back as a JSON array.</p>
	 *
	 * @param request  the {@code HttpServletRequest} object that contains the request the client made to the servlet
	 * @param response the {@code HttpServletResponse} object that contains the response the servlet returns to the client
	 * @throws ServletException if the request could not be handled
	 * @throws IOException if an input or output error occurs while the servlet is handling the request
	 */
	@Override
	@SuppressWarnings({ "java:S1989", "java:S3776", "unchecked", "deprecation" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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

	/**
	 * Builds a {@link DiscoverResponse} object from a given {@link DiscoverDto} and modality type.
	 *
	 * <p>This method takes a {@link DiscoverDto} object and a modality type string as input.
	 * It creates a new {@link DiscoverResponse} object and populates it with values from the
	 * provided {@link DiscoverDto}. Additionally, it sets the digital ID based on the given modality type.</p>
	 *
	 * @param fingerDiscovery the {@link DiscoverDto} object containing discovery details.
	 * @param modality the modality type (e.g., "FIR", "IIR", "FACE") used to retrieve the digital ID.
	 * @return a {@link DiscoverResponse} object populated with the provided discovery details and digital ID.
	 */
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

	/**
	 * Retrieves the digital ID for a given modality type.
	 *
	 * <p>This method reads the appropriate digital ID file based on the modality type provided.
	 * It then processes the file content to extract the digital ID. If an error occurs during
	 * file reading or processing, the error is logged and the method returns null.</p>
	 *
	 * @param modalityType the type of modality (e.g., "FIR", "IIR", "FACE") for which the digital ID is required.
	 * @return the digital ID as a Base64 URL encoded string, or null if an error occurs.
	 */
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

	/**
	 * Constructs a digital modality map from the given digital ID map, adds a
	 * timestamp, and encodes the resulting map into a Base64 URL-safe string.
	 *
	 * <p>
	 * This method creates a new LinkedHashMap to store the digital modality
	 * details, ensuring the order of insertion is preserved. It adds a timestamp to
	 * the map using the {@link #getTimeStamp()} method and includes various
	 * properties from the provided digital ID map. Finally, it converts the digital
	 * modality map to a JSON byte array and encodes it as a Base64 URL-safe string.
	 * </p>
	 *
	 * @param digitalIdMap a map containing digital ID information. Expected keys
	 *                     include: "deviceProvider", "deviceProviderId", "make",
	 *                     "serialNo", "model", "deviceSubType", and "type".
	 * @return a Base64 URL-safe encoded string representing the digital modality
	 *         map.
	 * @throws JsonProcessingException if there is an error processing the JSON
	 *                                 content.
	 */
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

	/**
	 * Retrieves the current timestamp in a specific format with timezone
	 * information.
	 *
	 * @return the formatted timestamp string
	 */
	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";
	}
}