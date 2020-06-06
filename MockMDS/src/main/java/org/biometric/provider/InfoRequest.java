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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceRequest;
import io.mosip.registration.mdm.dto.DigitalId;

public class InfoRequest extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private CryptoCore jwsValidation;

	InfoRequest(CryptoCore cryptoCore) {
		this.jwsValidation = cryptoCore;
	}

	ObjectMapper oB = null;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// if(req.getMethod().contentEquals("MOSIPDINFO"))
		if (req.getMethod().contentEquals("MOSIPDINFO") || req.getMethod().contentEquals("GET"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("GET"))
			CORSManager.doOptions(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (oB == null)
			oB = new ObjectMapper();
		oB.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		oB.setVisibilityChecker(
				VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
		List<String> listOfModalities = Arrays.asList("FIR", "IIR", "FACE");

		Map<String, Object> errorMap = new HashMap<>();
		List<Map<String, Object>> infoList = new ArrayList<Map<String, Object>>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "No Action Necessary.");

		listOfModalities.forEach(value -> {
			Map<String, Object> data = new HashMap<>();
			DeviceRequest deviceInfo = null;
			try {
				deviceInfo = oB.readValue(
						new String(Files.readAllBytes(Paths
								.get(System.getProperty("user.dir") + "/files/MockMDS/DeviceInfo" + value + ".txt"))),
						DeviceRequest.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			DeviceInfo info = new DeviceInfo();
			info.setCallbackId(deviceInfo.getCallbackId());
			info.setCertification(deviceInfo.getCertification());
			info.setComplianceLevel(deviceInfo.getComplianceLevel());
			info.setDeviceCode(deviceInfo.getDeviceCode());
			info.setDeviceExpiryDate(deviceInfo.getDeviceExpiryDate());
			info.setDeviceId(deviceInfo.getDeviceId());
			info.setDeviceInfoSignature(deviceInfo.getDeviceInfoSignature());
			info.setDeviceProcessName(deviceInfo.getDeviceProcessName());
			info.setDeviceProviderName(deviceInfo.getDeviceProviderName());
			info.setDeviceServiceId(deviceInfo.getDeviceServiceId());
			info.setDeviceStatus(deviceInfo.getStatus());
			info.setDeviceSubId(deviceInfo.getDeviceSubId());
			info.setDeviceSubType(deviceInfo.getDeviceSubType());
			info.setDeviceTimestamp(getTimeStamp());
			info.setDeviceType(deviceInfo.getDeviceType());
			info.setDeviceTypeName(deviceInfo.getDeviceTypeName());
			info.setDigitalId(getDigitalId(value));
			info.setVendorId(deviceInfo.getVendorId());
			info.setStatus(deviceInfo.getStatus());
			info.setSpecVersion(deviceInfo.getSpecVersion());
			info.setServiceVersion(deviceInfo.getServiceVersion());
			info.setSerialNo(deviceInfo.getSerialNo());
			info.setPurpose(deviceInfo.getPurpose());
			info.setProductId(deviceInfo.getProductId());
			info.setModel(deviceInfo.getModel());
			info.setHostId(deviceInfo.getHostId());
			info.setFirmware(deviceInfo.getFirmware());

			try {
				data.put("deviceInfo", getJwsPart(oB.writeValueAsString(info).getBytes()));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			data.put("error", errorMap);
			infoList.add(data);
		});

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONArray(infoList));
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

	private String getDigitalId(String type) {

		return getDigitalFingerId(type);

	}

	private String getTimeStamp() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+13"));

		return formatter.format(calendar.getTime()) + "+05:30";

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

		DigitalId digitalId = new DigitalId();
		digitalId.setDateTime(getTimeStamp());
		digitalId.setDeviceProvider(digitalIdMap.get("deviceProvider"));
		digitalId.setDeviceProviderId(digitalIdMap.get("deviceProviderId"));
		digitalId.setMake(digitalIdMap.get("make"));
		digitalId.setSerialNo(digitalIdMap.get("serialNo"));
		digitalId.setModel(digitalIdMap.get("model"));
		digitalId.setSubType(digitalIdMap.get("subType"));
		digitalId.setType(digitalIdMap.get("type"));

		return Base64.getEncoder().encodeToString(digitalId.toString().getBytes());

	}

}