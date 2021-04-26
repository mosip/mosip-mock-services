package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailSbiDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.NewBioAuthDto;
import io.mosip.registration.mdm.dto.NewBioDto;
import io.mosip.registration.mdm.dto.SbiBioMetricsDataDto;
import io.mosip.registration.mdm.dto.SbiCaptureRequestDto;

public class CaptureRequest extends HttpServlet {

	private CryptoCore jwsValidation;

	private String deviceVersion = null;
	
	CaptureRequest(CryptoCore cryptoCore, String deviceVersion) {
		this.jwsValidation = cryptoCore;
		this.deviceVersion = deviceVersion;
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
	private static final String errorCode = "errorCode";
	private static final String errorInfo = "errorInfo";
	private static final String error = "error";
	private static final String Auth = "Auth";
	private static final String Registration = "Registration";
	
	private static final List<String> environmentList=Arrays.asList("Staging","Developer","Pre-Production","Production");

	private static ObjectMapper oB = null;
	
	static {
		oB = new ObjectMapper();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("RCAPTURE") || req.getMethod().contentEquals("CAPTURE"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getMethod().contentEquals("RCAPTURE"))
			doRegistrationCapture(request, response);
		else if (request.getMethod().contentEquals("CAPTURE"))
			doAuthCapture(request, response);
		else // Handles the POST
			doRegistrationCapture(request, response);
	}
	
	private String getRequestString(HttpServletRequest request) throws Exception {
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
	protected void doRegistrationCapture(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		Map<String, Object> responseMap = new HashMap<>();
		try {
			
			if(!deviceVersion.equals("SBI 1.0")) {
				CaptureRequestDto captureRequestDto = (CaptureRequestDto) (oB
						.readValue(getRequestString(request).getBytes(), CaptureRequestDto.class));
				
				Map<String, Object> errorCountMap = new LinkedHashMap<>();
				errorCountMap.put(errorCode, "102");
				errorCountMap.put(errorInfo, "Count Mismatch");

				if (environmentList.contains(captureRequestDto.getEnv())
						&& captureRequestDto.getPurpose().equalsIgnoreCase(Registration)) {

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
								NewBioDto data = buildNewBioDto(dto, bio.type, bio.requestedScore,
										captureRequestDto.transactionId);
								Map<String, Object> biometricData = getMinimalResponse(captureRequestDto.specVersion, data,
										previousHash);
								listOfBiometric.add(biometricData);
								previousHash = (String) biometricData.get(HASH);
							}
						}
					}
					
					if(listOfBiometric.isEmpty()) {
						listOfBiometric.add(errorCountMap);
					}
					
					responseMap.put(BIOMETRICS, listOfBiometric);
					
				} else {
					Map<String, Object> errorMap = new LinkedHashMap<>();
					errorMap.put(errorCode, "101");
					errorMap.put(errorInfo, "Invalid Environment / Purpose");
					responseMap.put(error, errorMap);
				}
			}else {
				SbiCaptureRequestDto sbiCaptureRequestDto = (SbiCaptureRequestDto) (oB
						.readValue(getRequestString(request).getBytes(), SbiCaptureRequestDto.class));
				
				Map<String, Object> errorCountMap = new LinkedHashMap<>();
				errorCountMap.put(errorCode, "102");
				errorCountMap.put(errorInfo, "Count Mismatch");

				if (environmentList.contains(sbiCaptureRequestDto.getEnv())
						&& sbiCaptureRequestDto.getPurpose().equalsIgnoreCase(Registration)) {

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String previousHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes()));

					for (CaptureRequestDeviceDetailSbiDto bio : sbiCaptureRequestDto.getBio()) {
						List<SbiBioMetricsDataDto> list = new ArrayList<>();

						if (bio.getType().equals(FINGER))
							sbiCaptureFingersModality(bio, list);

						if (bio.getType().equals(IRIS))
							sbiCaptureIrisModality(bio, list);

						if (bio.getType().equals(FACE))
							sbiCaptureFaceModality(bio, list);
						
						if (!list.isEmpty()) {
							
							for (SbiBioMetricsDataDto dto : list) {
								NewBioDto data = buildNewSbiBioDto(dto, bio.type, bio.requestedScore,
										sbiCaptureRequestDto.getTransactionId());
								Map<String, Object> biometricData = getMinimalResponse(sbiCaptureRequestDto.getSpecVersion(), data,
										previousHash);
								listOfBiometric.add(biometricData);
								previousHash = (String) biometricData.get(HASH);
							}
						}
					}
					
					if(listOfBiometric.isEmpty()) {
						listOfBiometric.add(errorCountMap);
					}
					
					responseMap.put(BIOMETRICS, listOfBiometric);
					
				} else {
					Map<String, Object> errorMap = new LinkedHashMap<>();
					errorMap.put(errorCode, "101");
					errorMap.put(errorInfo, "Invalid Environment / Purpose");
					responseMap.put(error, errorMap);
				}
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));
	}
	
	
	protected void doAuthCapture(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		
		Map<String, Object> responseMap = new HashMap<>();
		
		try {
			CaptureRequestDto captureRequestDto = (CaptureRequestDto) (oB
					.readValue(getRequestString(request).getBytes(), CaptureRequestDto.class));

			if (environmentList.contains(captureRequestDto.getEnv())
					&& captureRequestDto.getPurpose().equalsIgnoreCase(Auth)) {

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
									java.util.Base64.getUrlDecoder().decode(dto.getBioValue()), captureRequestDto.transactionId);

							NewBioAuthDto data = buildAuthNewBioDto(dto, bio.type, bio.requestedScore,
									captureRequestDto.transactionId, result);
							Map<String, Object> biometricData = getAuthMinimalResponse(captureRequestDto.specVersion,
									data, previousHash, result, CryptoUtil.encodeBase64(JwtUtility.getCertificateThumbprint(certificate)));
							listOfBiometric.add(biometricData);
							previousHash = (String) biometricData.get(HASH);
						}
					}else {
						Map<String, Object> errorCountMap = new LinkedHashMap<>();
						errorCountMap.put(errorCode, "102");
						errorCountMap.put(errorInfo, "Count Mismatch");
						listOfBiometric.add(errorCountMap);
					}
				}

				responseMap.put(BIOMETRICS, listOfBiometric);

			} else {
				Map<String, Object> errorMap = new LinkedHashMap<>();
				errorMap.put(errorCode, "101");
				errorMap.put(errorInfo, "Invalid Environment / Purpose");
				responseMap.put(error, errorMap);
			}

		} catch (Exception exception) {

			exception.printStackTrace();

			Map<String, Object> errorMap = new LinkedHashMap<>();
			errorMap.put(errorCode, "UNKNOWN");
			errorMap.put(errorInfo, exception.getMessage());
			responseMap.put(error, errorMap);

		}
		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));

	}

	
	private void captureFaceModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list)
			throws IOException {
			
		BioMetricsDataDto dto = oB.readValue(Base64.getDecoder().decode(new String(Files.readAllBytes(
										Paths.get(System.getProperty("user.dir") + "/files/MockMDS/registration/Face.txt")))),
						BioMetricsDataDto.class);
		
		list.add(dto);
	}
	
	private void sbiCaptureFaceModality(CaptureRequestDeviceDetailSbiDto bio, List<SbiBioMetricsDataDto> list)
			throws IOException {
			
		SbiBioMetricsDataDto dto = oB.readValue(Base64.getDecoder().decode(new String(Files.readAllBytes(
										Paths.get(System.getProperty("user.dir") + "/SBIfiles/MockMDS/registration/Face.txt")))),
						SbiBioMetricsDataDto.class);
		
		list.add(dto);
	}

	
	private void captureIrisModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list) 
			throws JsonParseException, JsonMappingException, IOException {
		List<String> segmentsToCapture = null;

		switch (bio.deviceSubId) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Right"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "3": // both
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left", "Right"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "0": // not sure, need to check
			break;
		}

		if (segmentsToCapture == null || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		// TODO - validate requested Score, if deviceSubId is 3 then take the average of
		if (segmentsToCapture.size() == bio.getCount()) {
			for (String segment : segmentsToCapture) {
				BioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						BioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}
	}
	
	private void sbiCaptureIrisModality(CaptureRequestDeviceDetailSbiDto bio, List<SbiBioMetricsDataDto> list) 
			throws JsonParseException, JsonMappingException, IOException {
		List<String> segmentsToCapture = null;

		switch (bio.deviceSubId) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Right"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "3": // both
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left", "Right"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "0": // not sure, need to check
			break;
		}

		if (segmentsToCapture == null || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		// TODO - validate requested Score, if deviceSubId is 3 then take the average of
		if (segmentsToCapture.size() == bio.getCount()) {
			for (String segment : segmentsToCapture) {
				SbiBioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						SbiBioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}
	}
	
	private void captureFingersModality(CaptureRequestDeviceDetailDto bio, List<BioMetricsDataDto> list) 
			throws JsonParseException, JsonMappingException, IOException {

		List<String> segmentsToCapture = null;

		switch (bio.deviceSubId) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Left IndexFinger", "Left MiddleFinger", "Left RingFinger", "Left LittleFinger"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));

			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Right IndexFinger", "Right MiddleFinger", "Right RingFinger", "Right LittleFinger"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "3": // thumbs
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left Thumb", "Right Thumb"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "0":
			break;
		}

		if (segmentsToCapture == null || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		if (segmentsToCapture.size() == bio.getCount()) {
			// TODO - validate requested Score, if deviceSubId is 3 then take the average of

			for (String segment : segmentsToCapture) {
				BioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
										+ "/files/MockMDS/registration/" + segment + ".txt")))),
						BioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}

	}
	
	private void sbiCaptureFingersModality(CaptureRequestDeviceDetailSbiDto bio, List<SbiBioMetricsDataDto> list) 
			throws JsonParseException, JsonMappingException, IOException {

		List<String> segmentsToCapture = null;

		switch (bio.deviceSubId) {
		case "1": // left
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Left IndexFinger", "Left MiddleFinger", "Left RingFinger", "Left LittleFinger"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));

			break;

		case "2": // right
			segmentsToCapture = getSegmentsToCapture(
					Arrays.asList("Right IndexFinger", "Right MiddleFinger", "Right RingFinger", "Right LittleFinger"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "3": // thumbs
			segmentsToCapture = getSegmentsToCapture(Arrays.asList("Left Thumb", "Right Thumb"),
					bio.bioSubType == null ? null : Arrays.asList(bio.bioSubType),
					bio.exception == null ? null : Arrays.asList(bio.exception));
			break;

		case "0":
			break;
		}

		if (segmentsToCapture == null || segmentsToCapture.isEmpty()) {
			// Throw exception
		}

		if (segmentsToCapture.size() == bio.getCount()) {
			// TODO - validate requested Score, if deviceSubId is 3 then take the average of

			for (String segment : segmentsToCapture) {
				SbiBioMetricsDataDto bioMetricsData = oB.readValue(
						Base64.getDecoder()
								.decode(new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")
										+ "/SBIfiles/MockMDS/registration/" + segment + ".txt")))),
						SbiBioMetricsDataDto.class);
				list.add(bioMetricsData);
			}
		}

	}

	
	private NewBioDto buildNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, int requestedScore, String transactionId) {
		NewBioDto bioResponse = new NewBioDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bioType);
		
		if(bioMetricsData.getBioValue() != null)
			bioResponse.setBioValue(bioMetricsData.getBioValue());
		
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		//TODO Device service version should be read from file
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());
		
		//TODO - need to change, should handle based on deviceId
		bioResponse.setDigitalId(getDigitalId(bioType));
		
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(requestedScore);
		bioResponse.setQualityScore(bioMetricsData.getQualityScore());
		bioResponse.setTimestamp(CryptoUtility.getTimestamp());
		bioResponse.setTransactionId(transactionId);
		return bioResponse;
	}
	
	private NewBioDto buildNewSbiBioDto(SbiBioMetricsDataDto bioMetricsData, String bioType, int requestedScore, String transactionId) {
		NewBioDto bioResponse = new NewBioDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bioType);
		
		if(bioMetricsData.getBioValue() != null)
			bioResponse.setBioValue(bioMetricsData.getBioValue());
		
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		//TODO Device service version should be read from file
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());
		
		//TODO - need to change, should handle based on deviceId
		bioResponse.setDigitalId(getDigitalId(bioType));
		
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(requestedScore);
		bioResponse.setQualityScore(bioMetricsData.getQualityScore());
		bioResponse.setTimestamp(CryptoUtility.getTimestamp());
		bioResponse.setTransactionId(transactionId);
		return bioResponse;
	}
	
	
	private Map<String, Object> getMinimalResponse(String specVersion, NewBioDto data, String previousHash) {
		Map<String, Object> biometricData = new LinkedHashMap<>();
		try {			
			biometricData.put(SPEC_VERSION, specVersion);
			String dataBlock = JwtUtility.getJwt(oB.writeValueAsBytes(data), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
			biometricData.put(DATA, dataBlock);
			String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));
			String concatenatedHash = previousHash + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));
			biometricData.put(HASH, finalHash);
			biometricData.put(error, null);
		} catch (Exception ex) {
			ex.printStackTrace();
			Map<String, String> map = new HashMap<String, String>();
			map.put(errorCode, "UNKNOWN");
			map.put(errorInfo, ex.getMessage());
			biometricData.put(error, map);
		}		
		return biometricData;
	}
	
	// count, bioSubTypes and exceptions
	// if bioSubTypes is none -> then capture all segments based on count and exceptions
	// if bioSubTypes is not none -> then check if all bioSubTypes part of default if not throw error
	//								if UNKNOWN found, then choose one from default and make sure its not part of bioSubTypes/exceptions already
	//								again based on count
	private List<String> getSegmentsToCapture(List<String> defaultSubTypes, List<String> bioSubTypes, List<String> exceptions) {
		List<String> localCopy = new ArrayList<>();
		localCopy.addAll(defaultSubTypes);
		if(exceptions != null) {
			localCopy.removeAll(exceptions);
		}
		
		List<String> segmentsToCapture = new ArrayList<>();
		
		if(bioSubTypes == null || bioSubTypes.isEmpty()) {
			segmentsToCapture.addAll(localCopy);			
			return segmentsToCapture;
		}
		else {
			Random rand = new Random();
			for(String bioSubType : bioSubTypes) {
				if(localCopy.contains(bioSubType)) {
					segmentsToCapture.add(bioSubType);
				}
				else if("UNKNOWN".equals(bioSubType)) {
					String randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					while(bioSubTypes.contains(randSubType) && bioSubTypes.size() <= localCopy.size()) {
						randSubType = defaultSubTypes.get(rand.nextInt(defaultSubTypes.size()));
					}
					segmentsToCapture.add(randSubType);
				}
				else {
					//Throw exception
				}
			}
		}
		return segmentsToCapture;
	}
	
	
	
	private NewBioAuthDto buildAuthNewBioDto(BioMetricsDataDto bioMetricsData, String bioType, int requestedScore, String transactionId, 
			Map<String, String> cryptoResult) throws Exception {	
		
		NewBioAuthDto bioResponse = new NewBioAuthDto();
		bioResponse.setBioSubType(bioMetricsData.getBioSubType());
		bioResponse.setBioType(bioType);
		bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
		//TODO Device service version should be read from file
		bioResponse.setDeviceServiceVersion("MOSIP.MDS.001");
		bioResponse.setEnv(bioMetricsData.getEnv());
		//TODO - need to change, should handle based on deviceId
		bioResponse.setDigitalId(getDigitalId(bioType));		
		bioResponse.setPurpose(bioMetricsData.getPurpose());
		bioResponse.setRequestedScore(requestedScore);
		bioResponse.setQualityScore(bioMetricsData.getQualityScore());		
		bioResponse.setTransactionId(transactionId);
		//TODO Domain URL need to be set
		bioResponse.setDomainUri("");
		
		bioResponse.setTimestamp(cryptoResult.get("TIMESTAMP"));
		bioResponse.setBioValue(cryptoResult.containsKey("ENC_DATA") ? 
				cryptoResult.get("ENC_DATA") : null);		
		return bioResponse;
	}
	
	
	private Map<String, Object> getAuthMinimalResponse(String specVersion, NewBioAuthDto data, String previousHash, 
			Map<String, String> cryptoResult, String thumbprint) {
		Map<String, Object> biometricData = new LinkedHashMap<>();
		try {
			biometricData.put(SPEC_VERSION, specVersion);
			String dataAsString = oB.writeValueAsString(data);
			String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataAsString.getBytes(StandardCharsets.UTF_8)));
			String concatenatedHash = previousHash + presentHash;
			String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));
			biometricData.put(HASH, finalHash);
			biometricData.put(SESSION_KEY, cryptoResult.get("ENC_SESSION_KEY"));
			biometricData.put(THUMB_PRINT, thumbprint);
			biometricData.put(error, null);
			String dataBlock = JwtUtility.getJwt(dataAsString.getBytes(StandardCharsets.UTF_8), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
			biometricData.put(DATA, dataBlock);

		} catch (Exception ex) {
			ex.printStackTrace();
			Map<String, String> map = new HashMap<String, String>();
			map.put(errorCode, "UNKNOWN");
			map.put(errorInfo, ex.getMessage());
			biometricData.put(error, map);
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
		digitalMap.put("dateTime", CryptoUtility.getTimestamp());
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;

	}

}