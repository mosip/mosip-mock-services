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

/**
 * Handles MOSIPDINFO requests to provide device information encoded in JSON
 * format. This servlet processes POST requests and supports OPTIONS and GET
 * methods for CORS handling. It retrieves device information for modalities
 * FIR, IIR, and FACE, encodes them along with error details and sends them as
 * JSON arrays in the response.
 *
 * Uses X.509 certificates for digital signatures and JWT tokens for data
 * integrity. Handles file reading, cryptographic operations, and JSON
 * processing using Jackson ObjectMapper.
 *
 * @version 1.0
 */
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

	/**
	 * Constructs an InfoRequest object with the specified server port.
	 *
	 * @param serverPort the server port to be used
	 */
	public InfoRequest(String serverPort) {
		super();
		this.serverPort = serverPort;
	}

	/**
	 * Handles HTTP service requests. Routes MOSIPDINFO requests to doPost, OPTIONS
	 * to CORSManager.doOptions, and GET to CORSManager.doOptions.
	 *
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @throws ServletException if servlet-specific errors occur
	 * @throws IOException      if I/O errors occur
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (req.getMethod().contentEquals("MOSIPDINFO"))
			doPost(req, res);
		if (req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
		if (req.getMethod().contentEquals("GET"))
			CORSManager.doOptions(req, res);
	}

	/**
	 * Processes POST requests containing MOSIPDINFO. Retrieves device information
	 * for modalities FIR, IIR, and FACE, computes digital IDs, encodes data, and
	 * sends JSON array responses.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws IOException          if an I/O error occurs during request handling
	 * @throws NullPointerException if X509Certificate is null during certificate
	 *                              retrieval
	 */
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

	/**
	 * Encodes header, device data, and signature into a URL-safe Base64 encoded
	 * string.
	 *
	 * @param header    the header data byte array
	 * @param data      the device information data byte array
	 * @param signature the cryptographic signature byte array
	 * @return the encoded device information string
	 */
	private String getEncodedDeviceInfo(byte[] header, byte[] data, byte[] signature) {
		Encoder encoder = Base64.getUrlEncoder();
		return "" + new String(encoder.encode(header)) + "." + new String(encoder.encode(data)) + "."
				+ new String(encoder.encode(signature));
	}

	/**
	 * Constructs a data header byte array using the provided X.509 certificate.
	 *
	 * @param certificate the X.509 certificate used to generate the data header
	 * @return the data header byte array
	 */
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

	/**
	 * Retrieves a cryptographic signature byte array using the private key
	 * associated with the provided X.509 certificate.
	 *
	 * @param data        the device information data byte array to be signed
	 * @param certificate the X.509 certificate used for signing
	 * @return the cryptographic signature byte array
	 * @throws NullPointerException if JWT bytes are null after signing operation
	 */
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

	/**
	 * Retrieves device information data byte array for the specified modality type
	 * (FIR, IIR, or FACE).
	 *
	 * @param value the modality type (FIR, IIR, or FACE)
	 * @return the device information data byte array
	 * @throws NullPointerException if device information data is null
	 */
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

	/**
	 * Retrieves the X.509 certificate used for cryptographic operations.
	 *
	 * @return the X.509 certificate
	 */
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

	/**
	 * Removes header and footer lines (such as "-----BEGIN CERTIFICATE-----" and
	 * "-----END CERTIFICATE-----") and whitespace characters from the provided
	 * string.
	 *
	 * @param pKey the input string from which to remove header, footer, and
	 *             whitespace
	 * @return the cleaned string with headers, footers, and whitespace removed
	 */
	@SuppressWarnings({ "java:S5852" })
	private String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

	/**
	 * Retrieves the digital identifier for a given modality type (FIR, IIR, FACE).
	 *
	 * @param type the modality type (FIR, IIR, FACE)
	 * @return the digital identifier string
	 */
	private String getDigitalId(String type) {
		return getDigitalFingerId(type);
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

	/**
	 * Retrieves the digital identifier for a given modality type using mock data.
	 *
	 * @param moralityType the modality type (FIR, IIR, FACE)
	 * @return the digital identifier string
	 */
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

	/**
	 * Retrieves the digital modality information as a JWT string.
	 *
	 * @param digitalIdMap the map containing digital modality information
	 * @return the JWT string representing digital modality information
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
			result = JwtUtility.getJwt(objMapper.writeValueAsBytes(digitalMap), JwtUtility.getPrivateKey(),
					JwtUtility.getCertificate());
		} catch (IOException ex) {
			logger.error("getDigitalModality", ex);
		}
		return result;
	}
}