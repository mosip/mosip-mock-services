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

	private static final SecureRandom rand = new SecureRandom();

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

	/**
	 * Overridden service method to handle incoming HTTP requests for biometric
	 * capture.
	 * 
	 * @param req The HttpServletRequest object representing the incoming request.
	 * @param res The HttpServletResponse object for sending responses back to the
	 *            client.
	 * @throws ServletException Thrown if a servlet-specific exception occurs.
	 * @throws IOException      Thrown if an I/O error occurs while processing the
	 *                          request.
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("RCAPTURE") || req.getMethod().contentEquals("CAPTURE"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
	}

	/**
	 * Overridden doPost method for handling POST requests related to biometric
	 * capture.
	 * 
	 * @param request  The HttpServletRequest object representing the incoming POST
	 *                 request.
	 * @param response The HttpServletResponse object for sending responses back to
	 *                 the client.
	 * @throws IOException Thrown if an I/O error occurs while processing the
	 *                     request.
	 * 
	 *                     @SuppressWarnings({ "java:S1989", "java:S2189" })
	 *                     suppresses potential warnings related to unchecked cast
	 *                     and unchecked call to getReader().
	 */
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

	/**
	 * Reads the entire request body content from the provided HttpServletRequest.
	 * 
	 * @param request The HttpServletRequest object containing the request body.
	 * @return A String representing the complete request body content.
	 * @throws IOException Thrown if an I/O error occurs while reading the request
	 *                     body.
	 */
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

	/**
	 * Handles incoming HTTP POST requests for capturing biometric data for user
	 * authentication. This method processes the request, performs authentication
	 * capture based on the requested modalities (finger, iris, or face), and
	 * returns a response containing the captured biometric data (encrypted) or
	 * error messages.
	 *
	 * @param request  The HttpServletRequest object representing the incoming POST
	 *                 request.
	 * @param response The HttpServletResponse object for sending responses back to
	 *                 the client.
	 * @throws IOException Thrown if an I/O error occurs while processing the
	 *                     request or reading files.
	 */
	@SuppressWarnings({ "java:S1989", "java:S3776", "deprecation", "removal" })
	protected void doAuthCapture(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> responseMap = new HashMap<>();

		try {
			CaptureRequestDto captureRequestDto = objMapper.readValue(getRequestString(request).getBytes(),
					CaptureRequestDto.class);

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

	/**
	 * Simulates capturing facial biometric data for authentication.
	 * 
	 * (In a real-world scenario, this method would interact with a face capture
	 * device to acquire the actual biometric data)
	 * 
	 * This method reads pre-configured facial biometric data (simulated) from a
	 * file located at "${user.dir}/files/MockMDS/registration/Face.txt" and adds it
	 * to the provided list.
	 * 
	 * @param bio  The CaptureRequestDeviceDetailDto object containing details about
	 *             the requested modality.
	 * @param list The List object to store the captured biometric data
	 *             (BioMetricsDataDto).
	 * @throws IOException Thrown if an I/O error occurs while reading the file.
	 */
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

	/**
	 * Simulates capturing iris biometric data for authentication based on the
	 * requested segments (left, right, or both).
	 * 
	 * (In a real-world scenario, this method would interact with an iris capture
	 * device to acquire the actual biometric data)
	 * 
	 * This method retrieves pre-configured iris biometric data (simulated) from
	 * files based on the requested segments ("left", "right", or "both") and device
	 * sub-ID ("1", "2", or "3").
	 * 
	 * - "1" corresponds to left iris capture - "2" corresponds to right iris
	 * capture - "3" corresponds to both left and right iris capture
	 * 
	 * The method uses the `getSegmentsToCapture` method (not shown) to determine
	 * the actual segments to capture based on request parameters and potential
	 * exceptions.
	 * 
	 * @param bio  The CaptureRequestDeviceDetailDto object containing details about
	 *             the requested modality (including device sub-ID, sub-type, and
	 *             exceptions).
	 * @param list The List object to store the captured biometric data
	 *             (BioMetricsDataDto).
	 * @throws IOException Thrown if an I/O error occurs while reading the files.
	 */
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

	/**
	 * Simulates capturing fingerprint biometric data for authentication based on
	 * the requested segments (individual fingers or thumbs).
	 * 
	 * (In a real-world scenario, this method would interact with a fingerprint
	 * capture device to acquire the actual biometric data)
	 * 
	 * This method retrieves pre-configured fingerprint biometric data (simulated)
	 * from files based on the requested segments and device sub-ID ("1", "2", or
	 * "3").
	 * 
	 * - "1" corresponds to capturing all fingers on the left hand (index, middle,
	 * ring, little) - "2" corresponds to capturing all fingers on the right hand
	 * (index, middle, ring, little) - "3" corresponds to capturing thumbs from both
	 * hands (left and right)
	 * 
	 * The method uses the `getSegmentsToCapture` method (not shown) to determine
	 * the actual segments to capture based on request parameters and potential
	 * exceptions.
	 * 
	 * @param bio  The CaptureRequestDeviceDetailDto object containing details about
	 *             the requested modality (including device sub-ID, sub-type, and
	 *             exceptions).
	 * @param list The List object to store the captured biometric data
	 *             (BioMetricsDataDto).
	 * @throws IOException Thrown if an I/O error occurs while reading the files.
	 */
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

	/**
	 * Builds a NewBioDto object containing captured biometric data details.
	 * 
	 * This method populates a NewBioDto object with information extracted from the
	 * provided BioMetricsDataDto and additional details about the capture request.
	 * 
	 * @param bioMetricsData The BioMetricsDataDto object containing captured
	 *                       biometric data.
	 * @param bioType        The type of biometric data captured (e.g., "FINGER",
	 *                       "IRIS", "FACE").
	 * @param requestedScore The requested score for the captured biometric data.
	 * @param transactionId  The unique identifier for the capture transaction.
	 * @return A NewBioDto object containing the constructed response data.
	 */
	private NewBioDto buildNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, String requestedScore,
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

	/**
	 * Builds a minimal response map containing captured biometric data or error
	 * information.
	 * 
	 * This method constructs a response map based on the provided NewBioDto object
	 * and a previous hash value (used for security purposes). The response map
	 * includes: - Specification version - Encrypted biometric data block (if
	 * successful) - Hashed data for security validation - Error information (if any
	 * exception occurs during processing)
	 * 
	 * @param specVersion  The specification version of the response.
	 * @param data         The NewBioDto object containing captured biometric data.
	 * @param previousHash The previous hash value used for security chaining
	 *                     (optional).
	 * @return A Map object containing the constructed minimal response data.
	 * @throws Exception Thrown if an error occurs while processing the data or
	 *                   generating the JWT.
	 * @deprecated This method is marked as deprecated, suggesting a better
	 *             alternative might exist.
	 */
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

	/**
	 * Determines the final list of biometric segments to capture based on request
	 * parameters and exceptions.
	 * 
	 * This method takes three lists as input: - `defaultSubTypes`: The default list
	 * of biometric segments to capture for the modality. - `bioSubTypes`: The list
	 * of specific segments requested in the capture request (optional). -
	 * `exceptions`: The list of segments to exclude from capture due to exceptions
	 * (optional).
	 * 
	 * The method performs the following steps: 1. Creates a local copy of the
	 * `defaultSubTypes` list. 2. If `exceptions` is not null, removes those
	 * segments from the local copy. 3. Initializes an empty list to store the final
	 * segments to capture (`segmentsToCapture`). 4. Checks if `bioSubTypes` is null
	 * or empty: - If yes, adds all segments from the local copy to
	 * `segmentsToCapture` and returns it. 5. Otherwise, iterates through each
	 * `bioSubType` in the request: - If the `bioSubType` exists in the local copy,
	 * adds it to `segmentsToCapture`. - If `bioSubType` is "UNKNOWN", selects a
	 * random segment from the local copy (excluding any already requested segments
	 * in `bioSubTypes`) and adds it. - Otherwise, throws an exception
	 * (implementation not shown here). 6. Returns the final list of segments to
	 * capture (`segmentsToCapture`).
	 * 
	 * count, bioSubTypes and exceptions if bioSubTypes is none -> then capture all
	 * segments based on count and exceptions if bioSubTypes is not none -> then
	 * check if all bioSubTypes part of default if not throw error if UNKNOWN found,
	 * then choose one from default and make sure its not part of
	 * bioSubTypes/exceptions already again based on count
	 * 
	 * @param defaultSubTypes The default list of biometric segments for the
	 *                        modality.
	 * @param bioSubTypes     The list of specific segments requested in the capture
	 *                        request (optional).
	 * @param exceptions      The list of segments to exclude from capture due to
	 *                        exceptions (optional).
	 * @return A List containing the final segments to capture.
	 * @throws Exception Thrown if an unsupported `bioSubType` is encountered.
	 */
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

	/**
	 * Builds a NewBioAuthDto object containing captured biometric data and
	 * encryption details.
	 * 
	 * This method populates a NewBioAuthDto object with information extracted from
	 * the provided BioMetricsDataDto, additional details about the capture request,
	 * and encryption results.
	 * 
	 * @param bioMetricsData The BioMetricsDataDto object containing captured
	 *                       biometric data.
	 * @param bioType        The type of biometric data captured (e.g., "FINGER",
	 *                       "IRIS", "FACE").
	 * @param requestedScore The requested score for the captured biometric data.
	 * @param transactionId  The unique identifier for the capture transaction.
	 * @param cryptoResult   A Map containing encryption results (timestamp and
	 *                       encrypted data).
	 * @return A NewBioAuthDto object containing the constructed response data.
	 */
	private NewBioAuthDto buildAuthNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, String requestedScore,
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

	/**
	 * Builds a minimal response map containing captured biometric data, encryption
	 * details, and security hashes.
	 * 
	 * This method constructs a response map based on the provided NewBioAuthDto
	 * object, a previous hash value (used for security chaining), encryption
	 * results, and a thumbprint. The response map includes: - Specification version
	 * - Encrypted biometric data block (as JWT) - Hashed data for security
	 * validation - Encryption session key - Thumbprint (optional, purpose depends
	 * on context) - Error information (if any exception occurs during processing)
	 * 
	 * @param specVersion  The specification version of the response.
	 * @param data         The NewBioAuthDto object containing captured biometric
	 *                     data and details.
	 * @param previousHash The previous hash value used for security chaining
	 *                     (optional).
	 * @param cryptoResult A Map containing encryption results (session key).
	 * @param thumbprint   The thumbprint value (optional, purpose depends on
	 *                     context).
	 * @return A Map object containing the constructed minimal response data.
	 * @throws Exception Thrown if an error occurs while processing the data,
	 *                   generating hashes, or creating the JWT.
	 */
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
	 * Retrieves a mock Digital ID based on the provided modality type (for
	 * simulation purposes).
	 * 
	 * (In a real-world scenario, this method would interact with a Digital ID
	 * service to retrieve the actual Digital ID for the user based on the modality
	 * used)
	 * 
	 * This method simulates retrieving a Digital ID from a file based on the
	 * modality type ("FINGER", "IRIS", "FACE"). The files
	 * (MockMDS/DigitalFingerId.txt, MockMDS/DigitalIrisId.txt,
	 * MockMDS/DigitalFaceId.txt) are assumed to contain pre-configured Digital ID
	 * values for each modality.
	 * 
	 * The method uses Jackson's ObjectMapper to read the content of the respective
	 * file as a Map object and then calls a (presumably) helper method
	 * `getDigitalModality` (not shown) to extract the actual Digital ID.
	 * 
	 * @param modalityType The type of biometric modality for which to retrieve the
	 *                     Digital ID (e.g., "FINGER", "IRIS", "FACE").
	 * @return A String containing the retrieved Digital ID (or null if an error
	 *         occurs).
	 * @throws Exception Thrown if an IOException occurs while reading the file or
	 *                   any other unexpected error.
	 */
	@SuppressWarnings("unchecked")
	public String getDigitalId(String modalityType) {
		String digitalId = null;

		try {
			switch (modalityType) {
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
	 * Constructs a JWT token containing Digital ID details (for simulation
	 * purposes).
	 * 
	 * (In a real-world scenario, this method might not be necessary, as the actual
	 * Digital ID service might provide a response structure)
	 * 
	 * This method extracts relevant information from the provided Digital ID map
	 * (presumably retrieved from a file) and constructs a JWT token containing
	 * those details. The JWT payload includes: - Timestamp (generated using
	 * CryptoUtility) - Device provider information (copied from the input map) -
	 * Device details (make, serial number, model) - Device sub-type and type
	 * (copied from the input map)
	 * 
	 * The method uses Jackson's ObjectMapper to convert the digital map to a byte
	 * array before creating the JWT using JwtUtility.
	 * 
	 * @param digitalIdMap A Map containing Digital ID details (assumed to be
	 *                     retrieved from a file).
	 * @return A String containing the constructed JWT token (or null if an
	 *         IOException occurs).
	 * @throws IOException Thrown if an error occurs while converting the map to a
	 *                     byte array.
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