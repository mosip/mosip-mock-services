package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.NewBioAuthDto;
import io.mosip.registration.mdm.dto.NewBioDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CaptureRequest extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(CaptureRequest.class);
	/** User Dir. */
	public static final String USER_DIR = "user.dir";

	private static final SecureRandom rand = new SecureRandom ();

	private static final long serialVersionUID = -7250199164515356577L;

	private static final String FACE = "Face";
	private static final String IRIS = "Iris";
	private static final String FINGER = "Finger";
	private static final String SPEC_VERSION = "specVersion";
	private static final String DATA = "data";
	private static final String HASH = "hash";
	private static final String SESSION_KEY = "sessionKey";
	private static final String THUMB_PRINT = "thumbprint";
	private static final String BIOMETRICS = "biometrics";
	private static final String ERROR_CODE = "errorCode";
	private static final String ERROR_INFO = "errorInfo";
	private static final String ERROR = "error";
	private static final String AUTH = "Auth";
	private static final String REGISTRATION = "Registration";
	private static final String UNKNOWN = "UNKNOWN";

	private static final List<String> environmentList = Arrays.asList("Staging", "Developer", "Pre-Production",
			"Production");

	private static ObjectMapper objMapper = null;

	static {
		objMapper = new ObjectMapper();
	}

	public CaptureRequest() {
		super();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("RCAPTURE") || req.getMethod().contentEquals("CAPTURE"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
	}

	@Override
	@SuppressWarnings({ "java:S1989", "java:S2189" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getMethod().contentEquals("RCAPTURE"))
			doRegistrationCapture(request, response);
		else if (request.getMethod().contentEquals("CAPTURE"))
			doAuthCapture(request, response);
		else // Handles the POST
			doRegistrationCapture(request, response);
	}

	private String getRequestString(HttpServletRequest request) throws IOException {
		BufferedReader bR = request.getReader();
		String s = "";
		StringBuilder builder = new StringBuilder();
		while ((s = bR.readLine()) != null) {
			builder.append(s);
		}
		return builder.toString();
	}

	/**
	 * Do registration capture.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "java:S1989", "java:S3776", "deprecation" })
	protected void doRegistrationCapture(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			CaptureRequestDto captureRequestDto = objMapper.readValue(getRequestString(request).getBytes(),
					CaptureRequestDto.class);

			Map<String, Object> errorCountMap = new LinkedHashMap<>();
			errorCountMap.put(ERROR_CODE, "102");
			errorCountMap.put(ERROR_INFO, "Count Mismatch");

			if (environmentList.contains(captureRequestDto.getEnv())
					&& captureRequestDto.getPurpose().equalsIgnoreCase(REGISTRATION)) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String previousHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes()));

				for (CaptureRequestDeviceDetailDto bio : captureRequestDto.getBio()) {
					List<BioMetricsDataDto> list = new ArrayList<>();

					if (bio.getType().equals(FINGER))
						captureFingersModality(bio, list);

					if (bio.getType().equals(IRIS))
						captureIrisModality(bio, list);

					if (bio.getType().equals(FACE))
						captureFaceModality(bio, list);
					if (!list.isEmpty()) {

						for (BioMetricsDataDto dto : list) {
							NewBioDto data = buildNewBioDto(dto, bio.getType(), bio.getRequestedScore(),
									captureRequestDto.getTransactionId());
							Map<String, Object> biometricData = getMinimalResponse(captureRequestDto.getSpecVersion(),
									data, previousHash);
							listOfBiometric.add(biometricData);
							previousHash = (String) biometricData.get(HASH);
						}
					}
				}

				if (listOfBiometric.isEmpty()) {
					listOfBiometric.add(errorCountMap);
				}

				responseMap.put(BIOMETRICS, listOfBiometric);

			} else {
				Map<String, Object> errorMap = new LinkedHashMap<>();
				errorMap.put(ERROR_CODE, "101");
				errorMap.put(ERROR_INFO, "Invalid Environment / Purpose");
				responseMap.put(ERROR, errorMap);
			}
		} catch (Exception e) {
			logger.info("doRegistrationCapture", e);
		}

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));
	}

	@SuppressWarnings({ "java:S1989", "java:S3776", "deprecation", "removal" })
	protected void doAuthCapture(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();

		try {
			CaptureRequestDto captureRequestDto = objMapper
					.readValue(getRequestString(request).getBytes(), CaptureRequestDto.class);

			if (environmentList.contains(captureRequestDto.getEnv())
					&& captureRequestDto.getPurpose().equalsIgnoreCase(AUTH)) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String previousHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes()));

				for (CaptureRequestDeviceDetailDto bio : captureRequestDto.getBio()) {
					List<BioMetricsDataDto> list = new ArrayList<>();

					if (bio.getType().equals(FINGER))
						captureFingersModality(bio, list);

					if (bio.getType().equals(IRIS))
						captureIrisModality(bio, list);

					if (bio.getType().equals(FACE))
						captureFaceModality(bio, list);

					if (!list.isEmpty()) {

						for (BioMetricsDataDto dto : list) {
							X509Certificate certificate = new JwtUtility().getCertificateToEncryptCaptureBioValue();
							PublicKey publicKey = certificate.getPublicKey();
							Map<String, String> result = CryptoUtility.encrypt(publicKey,
									io.mosip.mock.sbi.util.StringHelper.base64UrlDecode(dto.getBioValue()),
									captureRequestDto.getTransactionId());

							NewBioAuthDto data = buildAuthNewBioDto(dto, bio.getType(), bio.getRequestedScore(),
									captureRequestDto.getTransactionId(), result);
							Map<String, Object> biometricData = getAuthMinimalResponse(
									captureRequestDto.getSpecVersion(), data, previousHash, result,
									CryptoUtil.encodeBase64(JwtUtility.getCertificateThumbprint(certificate)));
							listOfBiometric.add(biometricData);
							previousHash = (String) biometricData.get(HASH);
						}
					} else {
						Map<String, Object> errorCountMap = new LinkedHashMap<>();
						errorCountMap.put(ERROR_CODE, "102");
						errorCountMap.put(ERROR_INFO, "Count Mismatch");
						listOfBiometric.add(errorCountMap);
					}
				}

				responseMap.put(BIOMETRICS, listOfBiometric);
			} else {
				Map<String, Object> errorMap = new LinkedHashMap<>();
				errorMap.put(ERROR_CODE, "101");
				errorMap.put(ERROR_INFO, "Invalid Environment / Purpose");
				responseMap.put(ERROR, errorMap);
			}

		} catch (Exception e) {
			logger.info("doRegistrationCapture", e);

			Map<String, Object> errorMap = new LinkedHashMap<>();
			errorMap.put(ERROR_CODE, UNKNOWN);
			errorMap.put(ERROR_INFO, e.getMessage());
			responseMap.put(ERROR, errorMap);
		}

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));
	}

	@SuppressWarnings({ "java:S1172" })
	private void captureFaceModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list)
			throws IOException {
		BioMetricsDataDto dto = objMapper
				.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths
										.get(System.getProperty(USER_DIR) + "/files/MockMDS/registration/Face.txt")))),
						BioMetricsDataDto.class);

		list.add(dto);
	}

	@SuppressWarnings({ "java:S3776" })
	private void captureIrisModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list)
			throws IOException {
		List<String> segmentsToCapture = null;

		switch (bio.getDeviceSubId()) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Right"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		case "3": // both
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left", "Right"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		default: // not sure, need to check
			break;
		}

		if (Objects.isNull(segmentsToCapture) || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		if (!Objects.isNull(segmentsToCapture) && segmentsToCapture.size() == Integer.parseInt(bio.getCount())) {
			for (String segment : segmentsToCapture) {
				BioMetricsDataDto bioMetricsData = objMapper.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty(USER_DIR)
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						BioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}
	}

	@SuppressWarnings({ "java:S3776" })
	private void captureFingersModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list)
			throws IOException {

		List<String> segmentsToCapture = null;

		switch (bio.getDeviceSubId()) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Left IndexFinger", "Left MiddleFinger", "Left RingFinger", "Left LittleFinger"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Right IndexFinger", "Right MiddleFinger", "Right RingFinger", "Right LittleFinger"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		case "3": // thumbs
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left Thumb", "Right Thumb"),
					Arrays.asList(Optional.ofNullable(bio.getBioSubType()).orElse(new String[0])),
					Arrays.asList(Optional.ofNullable(bio.getException()).orElse(new String[0])));
			break;

		default:
			break;
		}

		if (Objects.isNull(segmentsToCapture) || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		if (!Objects.isNull(segmentsToCapture) && segmentsToCapture.size() == Integer.parseInt(bio.getCount())) {
			for (String segment : segmentsToCapture) {
				BioMetricsDataDto bioMetricsData = objMapper.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty(USER_DIR)
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						BioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}
	}

	private NewBioDto buildNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, int requestedScore,
			String transactionId) {
		NewBioDto bioResponse = new NewBioDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bioType);

		if (bioMetricsData.getBioValue() != null)
			bioResponse.setBioValue(bioMetricsData.getBioValue());

		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());

		bioResponse.setDigitalId(getDigitalId(bioType));

		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(requestedScore + "");
		bioResponse.setQualityScore(bioMetricsData.getQualityScore() + "");
		bioResponse.setTimestamp(CryptoUtility.getTimestamp());
		bioResponse.setTransactionId(transactionId);
		return bioResponse;
	}

	@SuppressWarnings("deprecation")
	private Map<String, Object> getMinimalResponse(String specVersion, NewBioDto data, String previousHash) {
		Map<String, Object> biometricData = new LinkedHashMap<>();
		try {
			biometricData.put(SPEC_VERSION, specVersion);
			String dataBlock = JwtUtility.getJwt(objMapper.writeValueAsBytes(data), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
			biometricData.put(DATA, dataBlock);
			String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));
			String concatenatedHash = previousHash + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));
			biometricData.put(HASH, finalHash);
			biometricData.put(ERROR, null);
		} catch (Exception ex) {
			logger.info("getMinimalResponse", ex);
			Map<String, String> map = new HashMap<>();
			map.put(ERROR_CODE, UNKNOWN);
			map.put(ERROR_INFO, ex.getMessage());
			biometricData.put(ERROR, map);
		}
		return biometricData;
	}

	// count, bioSubTypes and exceptions
	// if bioSubTypes is none -> then capture all segments based on count and
	// exceptions
	// if bioSubTypes is not none -> then check if all bioSubTypes part of default
	// if not throw error
	// if UNKNOWN found, then choose one from default and make sure its not part of
	// bioSubTypes/exceptions already
	// again based on count
	@SuppressWarnings({ "java:S3776" })
	private List<String> getSegmentsToCapture(List<String> defaultSubTypes, List<String> bioSubTypes,
			List<String> exceptions) {
		List<String> localCopy = new ArrayList<>();
		localCopy.addAll(defaultSubTypes);
		if (!Objects.isNull(exceptions)) {
			localCopy.removeAll(exceptions);
		}

		List<String> segmentsToCapture = new ArrayList<>();
		if (Objects.isNull(bioSubTypes) || bioSubTypes.isEmpty()) {
			segmentsToCapture.addAll(localCopy);
			return segmentsToCapture;
		} else {
			for (String bioSubType : bioSubTypes) {
				if (localCopy.contains(bioSubType)) {
					segmentsToCapture.add(bioSubType);
				} else if (UNKNOWN.equals(bioSubType)) {
					String randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					while (bioSubTypes.contains(randSubType) && bioSubTypes.size() <= localCopy.size()) {
						randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					}
					segmentsToCapture.add(randSubType);
				} else {
					// Throw exception
				}
			}
		}
		return segmentsToCapture;
	}

	private NewBioAuthDto buildAuthNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, int requestedScore,
			String transactionId, Map<String, String> cryptoResult) {
		NewBioAuthDto bioResponse = new NewBioAuthDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bioType);
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());
		bioResponse.setDigitalId(getDigitalId(bioType));
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(requestedScore + "");
		bioResponse.setQualityScore(bioMetricsData.getQualityScore() + "");
		bioResponse.setTransactionId(transactionId);
		bioResponse.setDomainUri("");

		bioResponse.setTimestamp(cryptoResult.get("TIMESTAMP"));
		bioResponse.setBioValue(cryptoResult.containsKey("ENC_DATA") ? cryptoResult.get("ENC_DATA") : null);
		return bioResponse;
	}

	@SuppressWarnings("deprecation")
	private Map<String, Object> getAuthMinimalResponse(String specVersion, NewBioAuthDto data, String previousHash,
			Map<String, String> cryptoResult, String thumbprint) {
		Map<String, Object> biometricData = new LinkedHashMap<>();
		try {
			biometricData.put(SPEC_VERSION, specVersion);
			String dataAsString = objMapper.writeValueAsString(data);
			String presentHash = HMACUtils
					.digestAsPlainText(HMACUtils.generateHash(dataAsString.getBytes(StandardCharsets.UTF_8)));
			String concatenatedHash = previousHash + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));
			biometricData.put(HASH, finalHash);
			biometricData.put(SESSION_KEY, cryptoResult.get("ENC_SESSION_KEY"));
			biometricData.put(THUMB_PRINT, thumbprint);
			biometricData.put(ERROR, null);
			String dataBlock = JwtUtility.getJwt(dataAsString.getBytes(StandardCharsets.UTF_8),
					JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
			biometricData.put(DATA, dataBlock);

		} catch (Exception ex) {
			logger.info("getAuthMinimalResponse", ex);
			Map<String, String> map = new HashMap<>();
			map.put(ERROR_CODE, UNKNOWN);
			map.put(ERROR_INFO, ex.getMessage());
			biometricData.put(ERROR, map);
		}
		return biometricData;
	}

	/**
	 * Gets the digital finger id.
	 *
	 * @param moralityType the morality type
	 * @return the digital finger id
	 */
	@SuppressWarnings("unchecked")
	public String getDigitalId(String moralityType) {
		String digitalId = null;

		try {
			switch (moralityType) {
			case FINGER:
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalFingerId.txt"))),
						Map.class));
				break;
				
			case IRIS:
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalIrisId.txt"))),
						Map.class));
				break;
				
			case FACE:
				digitalId = getDigitalModality(objMapper.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DigitalFaceId.txt"))),
						Map.class));
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			logger.info("getDigitalId", e);
		}

		return digitalId;
	}

	/**
	 * Gets the digital modality.
	 *
	 * @param digitalIdMap the digital id map
	 * @return the digital modality
	 */
	private String getDigitalModality(Map<String, String> digitalIdMap) {
		String result = null;
		Map<String, String> digitalMap = new LinkedHashMap<>();
		digitalMap.put("dateTime", CryptoUtility.getTimestamp());
		digitalMap.put("deviceProvider", digitalIdMap.get("deviceProvider"));
		digitalMap.put("deviceProviderId", digitalIdMap.get("deviceProviderId"));
		digitalMap.put("make", digitalIdMap.get("make"));
		digitalMap.put("serialNo", digitalIdMap.get("serialNo"));
		digitalMap.put("model", digitalIdMap.get("model"));
		digitalMap.put("deviceSubType", digitalIdMap.get("deviceSubType"));
		digitalMap.put("type", digitalIdMap.get("type"));
		try {
			result = JwtUtility.getJwt(objMapper.writeValueAsBytes(digitalMap), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
		} catch (IOException e) {
			logger.info("getDigitalModality", e);
		}
		return result;
	}
}