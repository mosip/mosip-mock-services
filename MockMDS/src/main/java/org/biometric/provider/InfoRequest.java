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
import java.util.Base64.Encoder;

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
import io.mosip.registration.mdm.dto.DataHeader;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceRequest;

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

		X509Certificate certificate = getCertificate();
		byte[] headerData = getHeader(certificate);

		listOfModalities.forEach(value -> {
			Map<String, Object> data = new HashMap<>();
			
			byte[] deviceInfoData = getDeviceInfo(value);
			byte[] signature = getSignature(deviceInfoData, certificate);

			String encodedDeviceInfo = getEncodedDeviceInfo(headerData, deviceInfoData, signature);

			data.put("deviceInfo", encodedDeviceInfo);
			data.put("error", errorMap);
			infoList.add(data);
		});

		response.setContentType("application/json");
		response = CORSManager.setCors(response);
		PrintWriter out = response.getWriter();
		out.println(new JSONArray(infoList));
	}

	private String getEncodedDeviceInfo(byte[] header, byte[] data, byte[] signature)
	{
		Encoder encoder = Base64.getUrlEncoder();
		return "" + new String(encoder.encode(header)) + "." + new String(encoder.encode(data)) + "." + new String(encoder.encode(signature));
	}

	private byte[] getHeader(X509Certificate certificate)
	{
		byte[] headerData = null;
		DataHeader header = new DataHeader();
		try
		{
			header.x5c.add(new String(certificate.getEncoded()));
			headerData =  oB.writeValueAsString(header).getBytes();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return headerData;
	}

	private byte[] getSignature(byte[] data, X509Certificate certificate)
	{
		String jwt = null;
		try {
			
			FileInputStream pkeyfis = new FileInputStream(
					new File(System.getProperty("user.dir") + "/files/keys/PrivateKey.pem").getPath());

			String pKey = getFileContent(pkeyfis, "UTF-8");
			pKey = trimBeginEnd(pKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));

			jwt = jwsValidation.sign(data, privateKey, certificate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jwt.getBytes();
	}

	private byte[] getDeviceInfo(String value)
	{
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
			info.callbackId = deviceInfo.callbackId;
			info.certification = deviceInfo.certification;
			info.ComplianceLevel = deviceInfo.ComplianceLevel;
			info.deviceCode = deviceInfo.deviceCode;
			info.DeviceExpiryDate = deviceInfo.DeviceExpiryDate;
			info.deviceId = deviceInfo.deviceId;
			info.DeviceInfoSignature = deviceInfo.DeviceInfoSignature;
			info.DeviceProcessName = deviceInfo.DeviceProcessName;
			info.DeviceProviderName = deviceInfo.DeviceProviderName;
			info.DeviceServiceId = deviceInfo.DeviceServiceId;
			info.deviceStatus = deviceInfo.status;
			info.deviceSubId = deviceInfo.deviceSubId;
			info.DeviceSubType = deviceInfo.DeviceSubType;
			info.DeviceTimestamp = getTimeStamp();
			info.DeviceType = deviceInfo.DeviceType;
			info.DeviceTypeName = deviceInfo.DeviceTypeName;
			info.digitalId = getDigitalId(value);
			info.VendorId = deviceInfo.VendorId;
			info.status = deviceInfo.status;
			info.specVersion = deviceInfo.specVersion;
			info.serviceVersion = deviceInfo.serviceVersion;
			info.SerialNo = deviceInfo.SerialNo;
			info.purpose = deviceInfo.purpose;
			info.ProductId = deviceInfo.ProductId;
			info.Model = deviceInfo.Model;
			info.HostId = deviceInfo.HostId;
			info.firmware = deviceInfo.firmware;
			byte[] deviceInfoData = null;
			try
			{
				deviceInfoData =  oB.writeValueAsString(info).getBytes();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			return deviceInfoData;
	}

	private X509Certificate getCertificate()
	{
		String cert = null;
		X509Certificate certificate = null;
		try{
			FileInputStream certfis = new FileInputStream( new File(System.getProperty("user.dir") + "/files/keys/MosipTestCert.pem").getPath());
				
			cert = getFileContent(certfis, "UTF-8");
			cert = trimBeginEnd(cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			certificate = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return certificate;
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

		String result = null;
		Map<String, String> digitalMap = new LinkedHashMap<String, String>();
		digitalMap.put("dateTime", getTimeStamp());
		digitalMap.put("deviceProvider", digitalIdMap.get("deviceProvider"));
		digitalMap.put("deviceProviderId", digitalIdMap.get("deviceProviderId"));
		digitalMap.put("make", digitalIdMap.get("make"));
		digitalMap.put("serialNo", digitalIdMap.get("serialNo"));
		digitalMap.put("model", digitalIdMap.get("model"));
		digitalMap.put("deviceSubType", digitalIdMap.get("deviceSubType"));
		digitalMap.put("type", digitalIdMap.get("type"));
		try {
			result = Base64.getEncoder().encodeToString(oB.writeValueAsBytes(digitalMap));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

}