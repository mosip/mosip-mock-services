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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import io.mosip.registration.mdm.dto.DataHeader;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("deprecation")
public class InfoRequest extends HttpServlet {
	private static final long serialVersionUID = 4364648551576142479L;

	private static final Logger logger = LoggerFactory.getLogger(InfoRequest.class);
	/** User Dir. */
	public static final String USER_DIR = "user.dir";
	private final String serverPort;
	private static ObjectMapper objMapper = null;
	
	static {
		objMapper = new ObjectMapper();
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objMapper.setVisibilityChecker(
				VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
	}

	public InfoRequest(String serverPort) {
		super();
		this.serverPort = serverPort;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("MOSIPDINFO"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("GET"))
			CORSManager.doOptions(req, res);
	}

	@Override
	@SuppressWarnings({ "java:S1989", "deprecation" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, NullPointerException {
		List<String> listOfModalities = Arrays.asList("FIR", "IIR", "FACE");

		Map<String, Object> errorMap = new HashMap<>();
		List<Map<String, Object>> infoList = new ArrayList<>();
		errorMap.put("errorCode", "0");
		errorMap.put("errorInfo", "No Action Necessary.");

		X509Certificate certificate = getCertificate();
		if (Objects.isNull(certificate))
			throw new NullPointerException("X509Certificate is null");

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

	private String getEncodedDeviceInfo(byte[] header, byte[] data, byte[] signature) {
		Encoder encoder = Base64.getUrlEncoder();
		return "" + new String(encoder.encode(header)) + "." + new String(encoder.encode(data)) + "."
				+ new String(encoder.encode(signature));
	}

	private byte[] getHeader(X509Certificate certificate) {
		byte[] headerData = null;
		DataHeader header = new DataHeader();
		try {
			header.getX5c().add(new String(certificate.getEncoded()));
			headerData = objMapper.writeValueAsString(header).getBytes();
		} catch (NullPointerException | CertificateEncodingException | JsonProcessingException ex) {
			logger.error("getHeader", ex);
		}
		return headerData;
	}

	private byte[] getSignature(byte[] data, X509Certificate certificate) {
		String jwt = null;
		try {
			FileInputStream pkeyfis = new FileInputStream(
					new File(System.getProperty(USER_DIR) + "/files/keys/PrivateKey.pem").getPath());

			String pKey = getFileContent(pkeyfis, "UTF-8");
			pKey = trimBeginEnd(pKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));

			jwt = CryptoUtility.sign(data, privateKey, certificate);
			return jwt.getBytes();
		} catch (Exception ex) {
			logger.error("getSignature", ex);
		}
		throw new NullPointerException("getSignature::jwt bytes is null");
	}

	private byte[] getDeviceInfo(String value) {
		DeviceRequest deviceInfo = null;
		try {
			deviceInfo = objMapper.readValue(
					new String(Files.readAllBytes(
							Paths.get(System.getProperty(USER_DIR) + "/files/MockMDS/DeviceInfo" + value + ".txt"))),
					DeviceRequest.class);
		} catch (IOException ex) {
			logger.error("getDeviceInfo", ex);
		}

		if (Objects.isNull(deviceInfo))
			throw new NullPointerException("getDeviceInfo::deviceInfo is null");

		DeviceInfo info = new DeviceInfo();
		info.setCallbackId(String.format("http://127.0.0.1:%s/", serverPort));
		info.setEnv(deviceInfo.getEnv());
		info.setCertification(deviceInfo.getCertification());
		info.setDeviceCode(deviceInfo.getDeviceCode());
		info.setDeviceId(deviceInfo.getDeviceId());
		info.setDeviceStatus(deviceInfo.getDeviceStatus());
		info.setDeviceSubId(deviceInfo.getDeviceSubId());
		info.setDigitalId(getDigitalId(value));
		info.setSpecVersion(deviceInfo.getSpecVersion());
		info.setServiceVersion(deviceInfo.getServiceVersion());
		info.setPurpose(deviceInfo.getPurpose());
		info.setFirmware(deviceInfo.getFirmware());

		byte[] deviceInfoData = null;
		try {
			deviceInfoData = objMapper.writeValueAsString(info).getBytes();
		} catch (Exception ex) {
			logger.error("getDeviceInfo", ex);
		}
		return deviceInfoData;
	}

	private X509Certificate getCertificate() {
		String cert = null;
		X509Certificate certificate = null;
		try {
			FileInputStream certfis = new FileInputStream(
					new File(System.getProperty(USER_DIR) + "/files/keys/MosipTestCert.pem").getPath());

			cert = getFileContent(certfis, "UTF-8");
			cert = trimBeginEnd(cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			certificate = (X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
		} catch (Exception ex) {
			logger.error("getCertificate", ex);
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
		} catch (Exception ex) {
			logger.error("getDigitalFingerId", ex);
		}

		return digitalId;
	}

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
			result = JwtUtility.getJwt(objMapper.writeValueAsBytes(digitalMap), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
		} catch (IOException ex) {
			logger.error("getDigitalModality", ex);
		}
		return result;
	}
}