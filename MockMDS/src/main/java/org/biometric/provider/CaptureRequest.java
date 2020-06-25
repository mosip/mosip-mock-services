package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.NewBioDto;

public class CaptureRequest extends HttpServlet {

	private CryptoCore jwsValidation;

	CaptureRequest(CryptoCore cryptoCore) {
		this.jwsValidation = cryptoCore;
	}

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

	ObjectMapper oB = null;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// if(req.getMethod().contentEquals("RCAPTURE") ||
		// req.getMethod().contentEquals("CAPTURE"))
		if (req.getMethod().contentEquals("RCAPTURE") || req.getMethod().contentEquals("CAPTURE")
				|| req.getMethod().contentEquals("POST"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getMethod().contentEquals("RCAPTURE"))
			doRegistrationCapture(request, response);
		else if (request.getMethod().contentEquals("CAPTURE"))
			doAuthCapture(request, response);
		else // Handles the POST
			doRegistrationCapture(request, response);
	}

	/**
	 * Do registration capture.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 */
	protected void doRegistrationCapture(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (oB == null)
			oB = new ObjectMapper();
		BufferedReader bR = request.getReader();
		String s = "";
		String sT = "";
		while ((s = bR.readLine()) != null) {
			sT = sT + s;
		}
		CaptureRequestDto captureRequestDto = (CaptureRequestDto) (oB.readValue(sT.getBytes(),
				CaptureRequestDto.class));
		CaptureRequestDeviceDetailDto bio = captureRequestDto.mosipBioRequest.get(0);
		Map<String, Object> responseMap = new HashMap<>();

		try {
			if (bio.getType().equalsIgnoreCase(FINGER))
				responseMap = getFingersModality(bio, captureRequestDto);
			if (bio.getType().equalsIgnoreCase(IRIS))
				responseMap = getIrisModality(bio, captureRequestDto);
			if (bio.getType().equalsIgnoreCase(FACE))
				responseMap = getFaceModality(bio, captureRequestDto);
		} catch (Exception exception) {

			exception.printStackTrace();

			Map<String, Object> errorMap = new LinkedHashMap<>();
			errorMap.put("errorCode", "101");
			errorMap.put("errorInfo", "Invalid JSON Value");
			responseMap.put("error", errorMap);

		}

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));
	}

	/**
	 * Gets the face modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the face modality
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> getFaceModality(CaptureRequestDeviceDetailDto bio, CaptureRequestDto captureRequestDto)
			throws IOException {
		Map<String, Object> responseMap = new LinkedHashMap<>();
		BioMetricsDataDto bioMetricsData = oB
				.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(
										Paths.get(System.getProperty("user.dir") + "/files/MockMDS/Face.txt")))),
						BioMetricsDataDto.class);

		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");

		List<Map<String, Object>> listOfBiometric = new ArrayList<>();
		String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

		Map<String, Object> data = new LinkedHashMap<>();
		NewBioDto bioResponse = new NewBioDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bio.type);
		bioResponse.setBioValue(bioMetricsData.getBioExtract());
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		//TODO Device service version should be read from file
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());
		bioResponse.setDigitalId(getDigitalFingerId(bio.type));
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(bio.requestedScore);
		bioResponse.setQualityScore(bioMetricsData.getQualityScore());
		bioResponse.setTimestamp(getTimeStamp());
		bioResponse.setTransactionId(captureRequestDto.getTransactionId());

		try {
			data.put(SPEC_VERSION, captureRequestDto.specVersion);

			if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
				Thread.sleep(captureRequestDto.timeout);

			String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
			data.put(DATA, dataBlock);

			String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

			String concatenatedHash = previousHashArray[0] + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

			data.put(HASH, finalHash);
			data.put("error", errorMap);
			listOfBiometric.add(data);
			previousHashArray[0] = finalHash;
		} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
				| CertificateException e) {
			e.printStackTrace();
		}

		responseMap.put(BIOMETRICS, listOfBiometric);
		return responseMap;
	}

	/**
	 * Gets the iris modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the iris modality
	 * @throws JsonParseException   the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException          Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> getIrisModality(CaptureRequestDeviceDetailDto bio, CaptureRequestDto captureRequestDto)
			throws JsonParseException, JsonMappingException, IOException {
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");
		if (bio.deviceId.equals("2") && bio.deviceSubId.equals("1")) {

			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			BioMetricsDataDto bioMetricsData = oB.readValue(
					Base64.getDecoder()
							.decode(new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Iris.txt")))),
					BioMetricsDataDto.class);
			Map<String, Object> data = new LinkedHashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			bioResponse.setBioType(bio.type);
			bioResponse.setBioValue(bioMetricsData.getBioExtract());
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			//TODO Device service version should be read from file
			bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.getTransactionId());

			try {
				data.put(SPEC_VERSION, captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
						JwtUtility.getCertificate());
				data.put(DATA, dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put(HASH, finalHash);
				data.put("error", errorMap);
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
					| CertificateException e) {
				e.printStackTrace();
			}

			responseMap.put(BIOMETRICS, listOfBiometric);

		} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("2")) {

			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			BioMetricsDataDto bioMetricsData = oB.readValue(
					Base64.getDecoder()
							.decode(new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Iris.txt")))),
					BioMetricsDataDto.class);
			Map<String, Object> data = new LinkedHashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			bioResponse.setBioType(bio.type);
			bioResponse.setBioValue(bioMetricsData.getBioExtract());
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			//TODO Device service version should be read from file
			bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.getTransactionId());

			try {

				data.put(SPEC_VERSION, captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
						JwtUtility.getCertificate());
				data.put(DATA, dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put(HASH, finalHash);
				data.put("error", errorMap);
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
					| CertificateException e) {
				e.printStackTrace();
			}

			responseMap.put(BIOMETRICS, listOfBiometric);

		} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("3")) {

			if (bio.bioSubType.length > 0) {

				List<String> dualIris = Arrays.asList(bio.bioSubType);

				dualIris.forEach(irisData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
												+ "/files/MockMDS/registration/" + irisData + ".txt")))),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
				bioMetricsDataDtoList.forEach(bioVal -> {
					Map<String, Object> data = new LinkedHashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					bioResponse.setBioType(bio.type);
					bioResponse.setBioValue(bioVal.getBioExtract());
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					bioResponse.setDeviceServiceVersion(bioVal.getDeviceServiceVersion());
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.getTransactionId());

					try {
						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
							| CertificateException | IOException e) {
						e.printStackTrace();
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);

			}

		}

		return responseMap;
	}

	/**
	 * Gets the fingers modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the fingers modality
	 */
	private Map<String, Object> getFingersModality(CaptureRequestDeviceDetailDto bio,
			CaptureRequestDto captureRequestDto) {
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");
		if (bio.deviceId.equals("1") && bio.deviceSubId.equals("1")) {

			if (bio.bioSubType.length > 0) {

				List<String> leftSlapFingers = Arrays.asList(bio.bioSubType);

				leftSlapFingers.forEach(leftData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
												+ "/files/MockMDS/registration/" + leftData + ".txt")))),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				bioMetricsDataDtoList.forEach(bioVal -> {
					Map<String, Object> data = new LinkedHashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioType(bio.type);
					bioResponse.setBioSubType(bioVal.getBioSubType());
					bioResponse.setBioValue(bioVal.getBioExtract());
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					bioResponse.setDeviceServiceVersion(bioVal.getDeviceServiceVersion());
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.getTransactionId());

					try {
						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (Exception e) {
						e.printStackTrace();
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);
			}

		} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("2")) {

			if (bio.bioSubType.length > 0) {

				List<String> rightSlapFingers = Arrays.asList(bio.bioSubType);

				rightSlapFingers.forEach(rightData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
												+ "/files/MockMDS/registration/" + rightData + "txt")))),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				bioMetricsDataDtoList.forEach(bioVal -> {
					Map<String, Object> data = new LinkedHashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					bioResponse.setBioType(bio.type);
					bioResponse.setBioValue(bioVal.getBioExtract());
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					bioResponse.setDeviceServiceVersion(bioVal.getDeviceServiceVersion());
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.getTransactionId());

					try {
						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
							| CertificateException | IOException e) {
						e.printStackTrace();
					}
				});
				responseMap.put(BIOMETRICS, listOfBiometric);
			}
		} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("3")) {

			if (bio.bioSubType.length > 0) {

				List<String> thumbFingers = Arrays.asList(bio.bioSubType);

				thumbFingers.forEach(thumbData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
												+ "/files/MockMDS/registration/" + thumbData + "txt")))),
								BioMetricsDataDto.class);

					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);
				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
				bioMetricsDataDtoList.forEach(bioVal -> {
					Map<String, Object> data = new LinkedHashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					bioResponse.setBioType(bio.type);
					bioResponse.setBioValue(bioVal.getBioExtract());
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					bioResponse.setDeviceServiceVersion(bioVal.getDeviceServiceVersion());
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.getTransactionId());

					try {
						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						listOfBiometric.add(data);
						data.put("error", errorMap);
						previousHashArray[0] = finalHash;
					} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
							| CertificateException | IOException e) {
						e.printStackTrace();
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);
			}
		}
		return responseMap;
	}

	/**
	 * Do auth capture.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 */
	protected void doAuthCapture(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (oB == null)
			oB = new ObjectMapper();
		BufferedReader bR = request.getReader();
		String s = "";
		String sT = "";
		while ((s = bR.readLine()) != null) {
			sT = sT + s;
		}
		CaptureRequestDto captureRequestDto = (CaptureRequestDto) (oB.readValue(sT.getBytes(),
				CaptureRequestDto.class));
		CaptureRequestDeviceDetailDto bio = captureRequestDto.mosipBioRequest.get(0);

		Map<String, Object> responseMap = new HashMap<>();
		try {
			if (bio.getType().equalsIgnoreCase(FINGER))
				responseMap = getAuthFingersModality(bio, captureRequestDto);
			if (bio.getType().equalsIgnoreCase(IRIS))
				responseMap = getAuthIrisModality(bio, captureRequestDto);
			if (bio.getType().equalsIgnoreCase(FACE))
				responseMap = getAuthFaceModality(bio, captureRequestDto);
		} catch (Exception exception) {

			exception.printStackTrace();

			Map<String, Object> errorMap = new LinkedHashMap<>();
			errorMap.put("errorCode", "101");
			errorMap.put("errorInfo", "Invalid JSON Value");
			responseMap.put("error", errorMap);

		}
		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));

	}

	/**
	 * Gets the auth face modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the auth face modality
	 * @throws JsonParseException   the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException          Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> getAuthFaceModality(CaptureRequestDeviceDetailDto bio,
			CaptureRequestDto captureRequestDto) throws JsonParseException, JsonMappingException, IOException {

		Map<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");
		BioMetricsDataDto bioMetricsData = oB.readValue(
				Base64.getDecoder()
						.decode(new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/Face.txt")))),
				BioMetricsDataDto.class);
		SecretKey sessionKey = MdsUtility.getSymmetricKey();
		List<Map<String, Object>> listOfBiometric = new ArrayList<>();
		String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

		Map<String, Object> data = new HashMap<>();
		NewBioDto bioResponse = new NewBioDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		try {
			bioResponse
					.setBioValue(MdsUtility.encryptedData(bioMetricsData.getBioExtract(), getTimeStamp(), sessionKey));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		//TODO Device service version should be read from file
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setBioType(bio.type);
		bioResponse.setEnv(bioMetricsData.getEnv());
		bioResponse.setDigitalId(getDigitalFingerId(bio.type));
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		//TODO Domain URL need to be set
		bioResponse.setDomainUri("");
		bioResponse.setRequestedScore(bio.requestedScore);
		bioResponse.setQualityScore(bioMetricsData.getQualityScore());
		bioResponse.setTimestamp(getTimeStamp());
		bioResponse.setTransactionId(captureRequestDto.getTransactionId());

		try {
			data.put(SPEC_VERSION, captureRequestDto.specVersion);

			if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
				Thread.sleep(captureRequestDto.timeout);

			String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
			data.put(DATA, dataBlock);

			String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

			String concatenatedHash = previousHashArray[0] + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

			data.put(HASH, finalHash);
			data.put(SESSION_KEY, Base64.getUrlEncoder()
					.encodeToString(MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
			data.put(THUMB_PRINT, "");
			data.put("error", errorMap);
			listOfBiometric.add(data);
			previousHashArray[0] = finalHash;
		} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
				| CertificateException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		responseMap.put(BIOMETRICS, listOfBiometric);

		return responseMap;
	}

	/**
	 * Gets the auth iris modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the auth iris modality
	 * @throws JsonParseException   the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException          Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> getAuthIrisModality(CaptureRequestDeviceDetailDto bio,
			CaptureRequestDto captureRequestDto) throws JsonParseException, JsonMappingException, IOException {
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");
		if (bio.deviceId.equals("2") && bio.deviceSubId.equals("1")) {
			SecretKey sessionKey = MdsUtility.getSymmetricKey();
			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			BioMetricsDataDto bioMetricsData = oB
					.readValue(
							Base64.getDecoder()
									.decode(new String(Files.readAllBytes(Paths
											.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Iris.txt")))),
							BioMetricsDataDto.class);
			Map<String, Object> data = new HashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			try {
				bioResponse.setBioValue(
						MdsUtility.encryptedData(bioMetricsData.getBioExtract(), getTimeStamp(), sessionKey));
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
					| NoSuchPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			//TODO Device service version should be read from file
			bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
			bioResponse.setBioType(bio.type);
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			//TODO Domain URL need to be set
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			bioResponse.setDomainUri("");
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.getTransactionId());

			try {
				data.put(SPEC_VERSION, captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
						JwtUtility.getCertificate());
				data.put(DATA, dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put(HASH, finalHash);
				data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
						MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
				data.put(THUMB_PRINT, "");
				data.put("error", errorMap);
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
					| CertificateException | InvalidKeyException | NoSuchPaddingException
					| InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}

			responseMap.put(BIOMETRICS, listOfBiometric);

		} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("2")) {
			SecretKey sessionKey = MdsUtility.getSymmetricKey();
			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			BioMetricsDataDto bioMetricsData = oB
					.readValue(
							Base64.getDecoder()
									.decode(new String(Files.readAllBytes(Paths
											.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Iris.txt")))),
							BioMetricsDataDto.class);
			Map<String, Object> data = new HashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			try {
				bioResponse.setBioValue(
						MdsUtility.encryptedData(bioMetricsData.getBioExtract(), getTimeStamp(), sessionKey));
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
					| NoSuchPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			//TODO Device service version should be read from file
			bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
			bioResponse.setBioType(bio.type);
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			//TODO Domain URL need to be set
			bioResponse.setDomainUri("");
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.transactionId);

			try {
				data.put(SPEC_VERSION, captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
						JwtUtility.getCertificate());
				data.put(DATA, dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put(HASH, finalHash);
				data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
						MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
				data.put(THUMB_PRINT, "");
				data.put("error", errorMap);
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
					| CertificateException | InvalidKeyException | NoSuchPaddingException
					| InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}

			responseMap.put(BIOMETRICS, listOfBiometric);

		} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("3")) {

			List<String> subTypeBothEyesArray = Arrays.asList(bio.bioSubType);

			subTypeBothEyesArray.forEach(bioData -> {

				BioMetricsDataDto bioMetricsData = null;
				try {
					bioMetricsData = oB.readValue(Base64.getDecoder().decode(getAuthFingers(bioData)),
							BioMetricsDataDto.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
				bioMetricsDataDtoList.add(bioMetricsData);

			});

			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
			bioMetricsDataDtoList.forEach(bioVal -> {
				SecretKey sessionKey = MdsUtility.getSymmetricKey();
				Map<String, Object> data = new HashMap<>();
				NewBioDto bioResponse = new NewBioDto();
				bioResponse.setBioSubType(bioVal.getBioSubType());
				try {
					bioResponse
							.setBioValue(MdsUtility.encryptedData(bioVal.getBioExtract(), getTimeStamp(), sessionKey));
				} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
						| NoSuchPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				bioResponse.setDeviceCode(bioVal.getDeviceCode());
				//TODO Device service version should be read from file
				bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
				bioResponse.setBioType(bio.type);
				bioResponse.setEnv(bioVal.getEnv());
				bioResponse.setDigitalId(getDigitalFingerId(bio.type));
				bioResponse.setPurpose(bioVal.getPurpose());
				//TODO Domain URL need to be set
				bioResponse.setDomainUri("");
				bioResponse.setRequestedScore(bio.requestedScore);
				bioResponse.setQualityScore(bioVal.getQualityScore());
				bioResponse.setTimestamp(getTimeStamp());
				bioResponse.setTransactionId(captureRequestDto.getTransactionId());

				try {
					data.put(SPEC_VERSION, captureRequestDto.specVersion);

					if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
						Thread.sleep(captureRequestDto.timeout);

					String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse), JwtUtility.getPrivateKey(),
							JwtUtility.getCertificate());
					data.put(DATA, dataBlock);

					String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

					String concatenatedHash = previousHashArray[0] + presentHash;
					String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

					data.put(HASH, finalHash);
					data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
							MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
					data.put(THUMB_PRINT, "");
					data.put("error", errorMap);
					listOfBiometric.add(data);
					previousHashArray[0] = finalHash;
				} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
						| CertificateException | IOException | InvalidKeyException | NoSuchPaddingException
						| InvalidAlgorithmParameterException e) {
					e.printStackTrace();
				}

			});
			responseMap.put(BIOMETRICS, listOfBiometric);

		}

		return responseMap;
	}

	/**
	 * Gets the auth fingers modality.
	 *
	 * @param bio               the bio
	 * @param captureRequestDto the capture request dto
	 * @return the auth fingers modality
	 */
	private Map<String, Object> getAuthFingersModality(CaptureRequestDeviceDetailDto bio,
			CaptureRequestDto captureRequestDto) {
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "Success");
		if (bio.type.equalsIgnoreCase(FINGER)) {
			if (bio.deviceId.equals("1") && bio.deviceSubId.equals("1")) {

				List<String> subTypeLeftSlabArray = Arrays.asList(bio.bioSubType);

				subTypeLeftSlabArray.forEach(bioData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(Base64.getDecoder().decode(getAuthFingers(bioData)),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				bioMetricsDataDtoList.forEach(bioVal -> {
					SecretKey sessionKey = MdsUtility.getSymmetricKey();
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					try {
						bioResponse.setBioValue(
								MdsUtility.encryptedData(bioVal.getBioExtract(), getTimeStamp(), sessionKey));
					} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					//TODO Device service version should be read from file
					bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
					bioResponse.setBioType(bio.type);
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					//TODO Domain URL need to be set
					bioResponse.setDomainUri("");
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.transactionId);

					try {

						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
								MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
						data.put(THUMB_PRINT, "");
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (Exception e) {
						e.printStackTrace();
						/*
						 * data.put("error", errorMap); listOfBiometric.add(e)
						 */
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);

			} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("2")) {

				List<String> subTypeRightSlabArray = Arrays.asList(bio.bioSubType);

				subTypeRightSlabArray.forEach(bioData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(Base64.getDecoder().decode(getAuthFingers(bioData)),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				bioMetricsDataDtoList.forEach(bioVal -> {
					SecretKey sessionKey = MdsUtility.getSymmetricKey();
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					try {
						bioResponse.setBioValue(
								MdsUtility.encryptedData(bioVal.getBioExtract(), getTimeStamp(), sessionKey));
					} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					//TODO Device service version should be read from file
					bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
					bioResponse.setBioType(bio.type);
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					//TODO Domain URL need to be set
					bioResponse.setDomainUri("");
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.transactionId);

					try {
						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
								MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
						data.put(THUMB_PRINT, "");
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
							| CertificateException | IOException | InvalidKeyException | NoSuchPaddingException
							| InvalidAlgorithmParameterException e) {
						e.printStackTrace();
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);

			} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("3")) {

				List<String> subTypeThumbsArray = Arrays.asList(bio.bioSubType);

				subTypeThumbsArray.forEach(bioData -> {

					BioMetricsDataDto bioMetricsData = null;
					try {
						bioMetricsData = oB.readValue(Base64.getDecoder().decode(getAuthFingers(bioData)),
								BioMetricsDataDto.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					bioMetricsDataDtoList.add(bioMetricsData);

				});

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
				bioMetricsDataDtoList.forEach(bioVal -> {
					SecretKey sessionKey = MdsUtility.getSymmetricKey();
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
					bioResponse.setBioSubType(bioVal.getBioSubType());
					try {
						bioResponse.setBioValue(
								MdsUtility.encryptedData(bioVal.getBioExtract(), getTimeStamp(), sessionKey));
					} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
							| NoSuchPaddingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					bioResponse.setDeviceCode(bioVal.getDeviceCode());
					//TODO Device service version should be read from file
					bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
					bioResponse.setBioType(bio.type);
					bioResponse.setEnv(bioVal.getEnv());
					bioResponse.setDigitalId(getDigitalFingerId(bio.type));
					bioResponse.setPurpose(bioVal.getPurpose());
					//TODO Domain URL need to be set
					bioResponse.setDomainUri("");
					bioResponse.setRequestedScore(bio.requestedScore);
					bioResponse.setQualityScore(bioVal.getQualityScore());
					bioResponse.setTimestamp(getTimeStamp());
					bioResponse.setTransactionId(captureRequestDto.transactionId);

					try {

						data.put(SPEC_VERSION, captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(bioResponse),
								JwtUtility.getPrivateKey(), JwtUtility.getCertificate());
						data.put(DATA, dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put(HASH, finalHash);
						data.put(SESSION_KEY, Base64.getUrlEncoder().encodeToString(
								MdsUtility.asymmetricEncrypt(JwtUtility.getPublicKey(), sessionKey.getEncoded())));
						data.put(THUMB_PRINT, "");
						data.put("error", errorMap);
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
							| CertificateException | IOException | InvalidKeyException | NoSuchPaddingException
							| InvalidAlgorithmParameterException e) {
						e.printStackTrace();
					}

				});
				responseMap.put(BIOMETRICS, listOfBiometric);
			}
		}
		return responseMap;
	}

	/**
	 * Gets the auth fingers.
	 *
	 * @param name the name
	 * @return the auth fingers
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String getAuthFingers(String name) throws IOException {

		switch (name) {
		case "Left IndexFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Index.txt")));
			break;
		case "Left MiddleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Middle.txt")));
			break;
		case "Left RingFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Ring.txt")));
			break;

		case "Left LittleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Little.txt")));
			break;
		case "Left Thumb":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Thumb.txt")));
			break;
		case "Right IndexFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Index.txt")));
			break;
		case "Right MiddleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Middle.txt")));
			break;

		case "Right RingFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Ring.txt")));
			break;
		case "Right LittleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Little.txt")));
			break;
		case "Right Thumb":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Thumb.txt")));
			break;
		case "Left":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/L_Iris.txt")));
			break;
		case "Right":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/auth/R_Iris.txt")));
			break;

		}
		return name;

	}

	/**
	 * Gets the time stamp.
	 *
	 * @return the time stamp
	 */
	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";

	}

	/**
	 * Gets the non exception bio.
	 *
	 * @param name the name
	 * @return the non exception bio
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getNonExceptionBio(String name) throws IOException {

		switch (name) {
		case "Left IndexFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Index.txt")));
			break;
		case "Left MiddleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Middle.txt")));
			break;
		case "Left RingFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Ring.txt")));
			break;

		case "Left LittleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Little.txt")));
			break;
		case "Left Thumb":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Thumb.txt")));
			break;
		case "Right IndexFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Index.txt")));
			break;
		case "Right MiddleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Middle.txt")));
			break;

		case "Right RingFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Ring.txt")));
			break;
		case "Right LittleFinger":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Little.txt")));
			break;
		case "Right Thumb":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Thumb.txt")));
			break;
		case "Left":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Iris.txt")));
			break;
		case "Right":
			name = new String(
					Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Iris.txt")));
			break;

		}
		return name;

	}

	/**
	 * Gets the digital finger id.
	 *
	 * @param moralityType the morality type
	 * @return the digital finger id
	 */
	@SuppressWarnings("unchecked")
	public String getDigitalFingerId(String moralityType) {

		String digitalId = null;

		try {
			switch (moralityType) {

			case FINGER:
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalFingerId.txt"))),
						Map.class));

				break;
			case IRIS:
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalIrisId.txt"))),
						Map.class));

				break;
			case FACE:
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

	/**
	 * Gets the digital modality.
	 *
	 * @param digitalIdMap the digital id map
	 * @return the digital modality
	 */
	private String getDigitalModality(Map<String, String> digitalIdMap) {

		String result = null;
		Map<String, String> digitalMap = new LinkedHashMap<>();
		digitalMap.put("dateTime", getTimeStamp());
		digitalMap.put("deviceProvider", digitalIdMap.get("deviceProvider"));
		digitalMap.put("deviceProviderId", digitalIdMap.get("deviceProviderId"));
		digitalMap.put("make", digitalIdMap.get("make"));
		digitalMap.put("serialNo", digitalIdMap.get("serialNo"));
		digitalMap.put("model", digitalIdMap.get("model"));
		digitalMap.put("deviceSubType", digitalIdMap.get("deviceSubType"));
		digitalMap.put("type", digitalIdMap.get("type"));
		try {
			result = JwtUtility.getJwt(oB.writeValueAsBytes(digitalMap), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | CertificateException | IOException e) {
			e.printStackTrace();
		}
		return result;

	}

}