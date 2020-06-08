package org.biometric.provider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
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

	private static final String startheader = "---BEGIN PRIVATE KEY---";
	private static final String endheader = "---END PRIVATE KEY---";

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
		String result = "";
		List<String> bioList = new ArrayList<>();
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		if (bio.type.equalsIgnoreCase("Finger")) {
			if (bio.deviceId.equals("1") && bio.deviceSubId.equals("1")) {

				List<String> noExceptionFingers = new ArrayList<>();
				noExceptionFingers.addAll(
						Arrays.asList("Left IndexFinger", "Left MiddleFinger", "Left RingFinger", "Left LittleFinger"));

				if (bio.exception.length == 0) {

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Little.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Ring.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Middle.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Index.txt"))));

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}
					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

					bioMetricsDataDtoList.forEach(bioVal -> {
						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (Exception e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);
				} else {

					List<String> exceptionFingers = Arrays.asList(bio.exception);

					getFingers(noExceptionFingers, exceptionFingers).forEach(obj -> {

						try {
							bioList.add(getNonExceptionBio(obj));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {

						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);
				}

			} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("2")) {

				List<String> noExceptionFingers = new ArrayList<>();
				noExceptionFingers.addAll(Arrays.asList("Right IndexFinger", "Right MiddleFinger", "Right RingFinger",
						"Right LittleFinger"));

				if (bio.exception.length == 0) {

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Little.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Ring.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Middle.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Index.txt"))));

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

					bioMetricsDataDtoList.forEach(bioVal -> {
						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);

				} else {

					List<String> exceptionFingers = Arrays.asList(bio.exception);

					getFingers(noExceptionFingers, exceptionFingers).forEach(obj -> {

						try {
							bioList.add(getNonExceptionBio(obj));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {

						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);
				}

			} else if (bio.deviceId.equals("1") && bio.deviceSubId.equals("3")) {

				List<String> noExceptionFingers = new ArrayList<>();
				noExceptionFingers.addAll(Arrays.asList("Right Thumb", "Left Thumb"));

				if (bio.exception.length == 0) {

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Thumb.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Thumb.txt"))));

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {
						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);

				} else {

					List<String> exceptionFingers = Arrays.asList(bio.exception);

					getFingers(noExceptionFingers, exceptionFingers).forEach(obj -> {

						try {
							bioList.add(getNonExceptionBio(obj));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {

						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);
				}

			}
		} else if (bio.type.equalsIgnoreCase("Iris")) {
			if (bio.deviceId.equals("2") && bio.deviceSubId.equals("1")) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				BioMetricsDataDto bioMetricsData = oB
						.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths
												.get(System.getProperty("user.dir") + "/files/MockMDS/L_Iris.txt")))),
								BioMetricsDataDto.class);
				Map<String, Object> data = new HashMap<>();
				NewBioDto bioResponse = new NewBioDto();
				bioResponse.setBioSubType(bioMetricsData.getBioSubType());
				bioResponse.setBioValue(bioMetricsData.getBioExtract());
				bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
				bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
				bioResponse.setEnv(bioMetricsData.getEnv());
				bioResponse.setDigitalId(getDigitalFingerId(bio.type));
				bioResponse.setPurpose(bioMetricsData.getPurpose());
				bioResponse.setRequestedScore(bio.requestedScore);
				bioResponse.setQualityScore(bioMetricsData.getQualityScore());
				bioResponse.setTimestamp(getTimeStamp());
				bioResponse.setTransactionId(captureRequestDto.getTransactionId());

				try {
					data.put("specVersion", captureRequestDto.specVersion);

					if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
						Thread.sleep(captureRequestDto.timeout);

					String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
					data.put("data", dataBlock);

					String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

					String concatenatedHash = previousHashArray[0] + presentHash;
					String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

					data.put("hash", finalHash);
					listOfBiometric.add(data);
					previousHashArray[0] = finalHash;
				} catch (JsonProcessingException | InterruptedException e) {
					e.printStackTrace();
				}

				responseMap.put("biometrics", listOfBiometric);

			} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("2")) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				BioMetricsDataDto bioMetricsData = oB
						.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths
												.get(System.getProperty("user.dir") + "/files/MockMDS/R_Iris.txt")))),
								BioMetricsDataDto.class);
				Map<String, Object> data = new HashMap<>();
				NewBioDto bioResponse = new NewBioDto();
				bioResponse.setBioSubType(bioMetricsData.getBioSubType());
				bioResponse.setBioValue(bioMetricsData.getBioExtract());
				bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
				bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
				bioResponse.setEnv(bioMetricsData.getEnv());
				bioResponse.setDigitalId(getDigitalFingerId(bio.type));
				bioResponse.setPurpose(bioMetricsData.getPurpose());
				bioResponse.setRequestedScore(bio.requestedScore);
				bioResponse.setQualityScore(bioMetricsData.getQualityScore());
				bioResponse.setTimestamp(getTimeStamp());
				bioResponse.setTransactionId(captureRequestDto.getTransactionId());

				try {
					data.put("specVersion", captureRequestDto.specVersion);

					if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
						Thread.sleep(captureRequestDto.timeout);

					String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
					data.put("data", dataBlock);

					String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

					String concatenatedHash = previousHashArray[0] + presentHash;
					String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

					data.put("hash", finalHash);
					listOfBiometric.add(data);
					previousHashArray[0] = finalHash;
				} catch (JsonProcessingException | InterruptedException e) {
					e.printStackTrace();
				}

				responseMap.put("biometrics", listOfBiometric);

			} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("3")) {

				List<String> noExceptionIris = new ArrayList<>();
				noExceptionIris.addAll(Arrays.asList("Left", "Right"));

				if (bio.exception.length == 0) {

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/L_Iris.txt"))));

					bioList.add(new String(Files
							.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/R_Iris.txt"))));

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {
						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);

				} else {

					List<String> exceptionIris = Arrays.asList(bio.exception);

					getFingers(noExceptionIris, exceptionIris).forEach(obj -> {

						try {
							bioList.add(getNonExceptionBio(obj));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});

					for (String bioData : bioList) {

						BioMetricsDataDto bioMetricsData = oB.readValue(Base64.getDecoder().decode(bioData),
								BioMetricsDataDto.class);
						bioMetricsDataDtoList.add(bioMetricsData);

					}

					List<Map<String, Object>> listOfBiometric = new ArrayList<>();
					String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };
					bioMetricsDataDtoList.forEach(bioVal -> {

						Map<String, Object> data = new HashMap<>();
						NewBioDto bioResponse = new NewBioDto();
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
							data.put("specVersion", captureRequestDto.specVersion);

							if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
								Thread.sleep(captureRequestDto.timeout);

							String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
							data.put("data", dataBlock);

							String presentHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

							String concatenatedHash = previousHashArray[0] + presentHash;
							String finalHash = HMACUtils
									.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

							data.put("hash", finalHash);
							listOfBiometric.add(data);
							previousHashArray[0] = finalHash;
						} catch (JsonProcessingException | InterruptedException e) {
							e.printStackTrace();
						}

					});
					responseMap.put("biometrics", listOfBiometric);
				}

			}
		} else if (bio.type.equalsIgnoreCase("Face")) {

			BioMetricsDataDto bioMetricsData = oB.readValue(
					Base64.getDecoder()
							.decode(new String(Files.readAllBytes(
									Paths.get(System.getProperty("user.dir") + "/files/MockMDS/Face.txt")))),
					BioMetricsDataDto.class);

			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			Map<String, Object> data = new HashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			bioResponse.setBioValue(bioMetricsData.getBioExtract());
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.getTransactionId());

			try {
				data.put("specVersion", captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
				data.put("data", dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put("hash", finalHash);
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException e) {
				e.printStackTrace();
			}

			responseMap.put("biometrics", listOfBiometric);

		}

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));
	}

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
		String result = "";

		List<String> bioList = new ArrayList<>();
		List<BioMetricsDataDto> bioMetricsDataDtoList = new ArrayList<>();
		Map<String, Object> responseMap = new LinkedHashMap<>();
		if (bio.type.equalsIgnoreCase("Finger")) {
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
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
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
						data.put("specVersion", captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
						data.put("data", dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put("hash", finalHash);
						data.put("sessionKey", "");
						data.put("thumbprint", "");
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (Exception e) {
						e.printStackTrace();
					}

				});
				responseMap.put("biometrics", listOfBiometric);

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
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
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
						data.put("specVersion", captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
						data.put("data", dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put("hash", finalHash);
						data.put("sessionKey", "");
						data.put("thumbprint", "");
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (JsonProcessingException | InterruptedException e) {
						e.printStackTrace();
					}

				});
				responseMap.put("biometrics", listOfBiometric);

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
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
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
						data.put("specVersion", captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
						data.put("data", dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put("hash", finalHash);
						data.put("sessionKey", "");
						data.put("thumbprint", "");
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (JsonProcessingException | InterruptedException e) {
						e.printStackTrace();
					}

				});
				responseMap.put("biometrics", listOfBiometric);

			}
		} else if (bio.type.equalsIgnoreCase("Iris")) {
			if (bio.deviceId.equals("2") && bio.deviceSubId.equals("1")) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				BioMetricsDataDto bioMetricsData = oB
						.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(
												System.getProperty("user.dir") + "/files/MockMDS/auth/L_Iris.txt")))),
								BioMetricsDataDto.class);
				Map<String, Object> data = new HashMap<>();
				NewBioDto bioResponse = new NewBioDto();
				bioResponse.setBioSubType(bioMetricsData.getBioSubType());
				bioResponse.setBioValue(bioMetricsData.getBioExtract());
				bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
				bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
				bioResponse.setEnv(bioMetricsData.getEnv());
				bioResponse.setDigitalId(getDigitalFingerId(bio.type));
				bioResponse.setPurpose(bioMetricsData.getPurpose());
				bioResponse.setRequestedScore(bio.requestedScore);
				bioResponse.setQualityScore(bioMetricsData.getQualityScore());
				bioResponse.setTimestamp(getTimeStamp());
				bioResponse.setTransactionId(captureRequestDto.getTransactionId());

				try {
					data.put("specVersion", captureRequestDto.specVersion);

					if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
						Thread.sleep(captureRequestDto.timeout);

					String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
					data.put("data", dataBlock);

					String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

					String concatenatedHash = previousHashArray[0] + presentHash;
					String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

					data.put("hash", finalHash);
					data.put("sessionKey", "");
					data.put("thumbprint", "");
					listOfBiometric.add(data);
					previousHashArray[0] = finalHash;
				} catch (JsonProcessingException | InterruptedException e) {
					e.printStackTrace();
				}

				responseMap.put("biometrics", listOfBiometric);

			} else if (bio.deviceId.equals("2") && bio.deviceSubId.equals("2")) {

				List<Map<String, Object>> listOfBiometric = new ArrayList<>();
				String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

				BioMetricsDataDto bioMetricsData = oB
						.readValue(
								Base64.getDecoder()
										.decode(new String(Files.readAllBytes(Paths.get(
												System.getProperty("user.dir") + "/files/MockMDS/auth/R_Iris.txt")))),
								BioMetricsDataDto.class);
				Map<String, Object> data = new HashMap<>();
				NewBioDto bioResponse = new NewBioDto();
				bioResponse.setBioSubType(bioMetricsData.getBioSubType());
				bioResponse.setBioValue(bioMetricsData.getBioExtract());
				bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
				bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
				bioResponse.setEnv(bioMetricsData.getEnv());
				bioResponse.setDigitalId(getDigitalFingerId(bio.type));
				bioResponse.setPurpose(bioMetricsData.getPurpose());
				bioResponse.setRequestedScore(bio.requestedScore);
				bioResponse.setQualityScore(bioMetricsData.getQualityScore());
				bioResponse.setTimestamp(getTimeStamp());
				bioResponse.setTransactionId(captureRequestDto.getTransactionId());

				try {
					data.put("specVersion", captureRequestDto.specVersion);

					if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
						Thread.sleep(captureRequestDto.timeout);

					String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
					data.put("data", dataBlock);

					String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

					String concatenatedHash = previousHashArray[0] + presentHash;
					String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

					data.put("hash", finalHash);
					data.put("sessionKey", "");
					data.put("thumbprint", "");
					listOfBiometric.add(data);
					previousHashArray[0] = finalHash;
				} catch (JsonProcessingException | InterruptedException e) {
					e.printStackTrace();
				}

				responseMap.put("biometrics", listOfBiometric);

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
					Map<String, Object> data = new HashMap<>();
					NewBioDto bioResponse = new NewBioDto();
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
						data.put("specVersion", captureRequestDto.specVersion);

						if (Integer.valueOf(bioVal.getQualityScore()) < bio.requestedScore)
							Thread.sleep(captureRequestDto.timeout);

						String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
						data.put("data", dataBlock);

						String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

						String concatenatedHash = previousHashArray[0] + presentHash;
						String finalHash = HMACUtils
								.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

						data.put("hash", finalHash);
						data.put("sessionKey", "");
						data.put("thumbprint", "");
						listOfBiometric.add(data);
						previousHashArray[0] = finalHash;
					} catch (JsonProcessingException | InterruptedException e) {
						e.printStackTrace();
					}

				});
				responseMap.put("biometrics", listOfBiometric);

			}
		} else if (bio.type.equalsIgnoreCase("Face")) {

			BioMetricsDataDto bioMetricsData = oB
					.readValue(
							Base64.getDecoder()
									.decode(new String(Files.readAllBytes(Paths
											.get(System.getProperty("user.dir") + "/files/MockMDS/auth/Face.txt")))),
							BioMetricsDataDto.class);

			List<Map<String, Object>> listOfBiometric = new ArrayList<>();
			String[] previousHashArray = { HMACUtils.digestAsPlainText(HMACUtils.generateHash("".getBytes())) };

			Map<String, Object> data = new HashMap<>();
			NewBioDto bioResponse = new NewBioDto();
			bioResponse.setBioSubType(bioMetricsData.getBioSubType());
			bioResponse.setBioValue(bioMetricsData.getBioExtract());
			bioResponse.setDeviceCode(bioMetricsData.getDeviceCode());
			bioResponse.setDeviceServiceVersion(bioMetricsData.getDeviceServiceVersion());
			bioResponse.setEnv(bioMetricsData.getEnv());
			bioResponse.setDigitalId(getDigitalFingerId(bio.type));
			bioResponse.setPurpose(bioMetricsData.getPurpose());
			bioResponse.setRequestedScore(bio.requestedScore);
			bioResponse.setQualityScore(bioMetricsData.getQualityScore());
			bioResponse.setTimestamp(getTimeStamp());
			bioResponse.setTransactionId(captureRequestDto.getTransactionId());

			try {
				data.put("specVersion", captureRequestDto.specVersion);

				if (Integer.valueOf(bioMetricsData.getQualityScore()) < bio.requestedScore)
					Thread.sleep(captureRequestDto.timeout);

				String dataBlock = getJwsPart(oB.writeValueAsString(bioResponse).getBytes());
				data.put("data", dataBlock);

				String presentHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(dataBlock.getBytes()));

				String concatenatedHash = previousHashArray[0] + presentHash;
				String finalHash = HMACUtils.digestAsPlainText(HMACUtils.generateHash(concatenatedHash.getBytes()));

				data.put("hash", finalHash);
				data.put("sessionKey", "");
				data.put("thumbprint", "");
				listOfBiometric.add(data);
				previousHashArray[0] = finalHash;
			} catch (JsonProcessingException | InterruptedException e) {
				e.printStackTrace();
			}

			responseMap.put("biometrics", listOfBiometric);

		}

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONObject(responseMap));

	}

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

	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";

	}

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

	List<String> getFingers(List<String> noExceptionList, List<String> exceptionList) {

		for (String restult : exceptionList) {

			noExceptionList.remove(restult);
		}

		return noExceptionList;

	}

	public String getJwsPart(byte[] data) {
		String jwt = null;
		try {

			FileInputStream pkeyfis = new FileInputStream(
					new File(System.getProperty("user.dir") + "/files/keys/PrivateKey.pem").getPath());

			String pKey = getFileContent(pkeyfis, "UTF-8");
			FileInputStream certfis = new FileInputStream(

					new File(System.getProperty("user.dir") + "/files/keys/MosipTestCert.pem").getPath());

			String cert = getFileContent(certfis, "UTF-8");
			pKey = trimBeginEnd(pKey);
			cert = trimBeginEnd(cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));

			jwt = jwsValidation.sign(data, privateKey, certificate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jwt;

	}

	/**
	 * Gets the file content.
	 *
	 * @param fis      the fis
	 * @param encoding the encoding
	 * @return the file content
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getFileContent(FileInputStream fis, String encoding) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
	}

	private String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

	@SuppressWarnings("unchecked")
	public String getDigitalFingerId(String moralityType) {

		String digitalId = null;

		try {
			switch (moralityType) {

			case "Finger":
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalFingerId.txt"))),
						Map.class));

				break;
			case "Iris":
				digitalId = getDigitalModality(oB.readValue(
						new String(Files.readAllBytes(
								Paths.get(System.getProperty("user.dir") + "/files/MockMDS/DigitalIrisId.txt"))),
						Map.class));

				break;
			case "Face":
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
}