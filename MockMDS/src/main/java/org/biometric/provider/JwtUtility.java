package org.biometric.provider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.web.reactive.function.client.ClientResponse;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.mdm.dto.CryptomanagerRequestDto;

import org.bouncycastle.asn1.eac.RSAPublicKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;


public class JwtUtility {
	
	
	static ObjectMapper mapper = new ObjectMapper();
	//TODO Need to be implement using properties 
	//@Value("${mosip.kernel.crypto.sign-algorithm-name:RS256}")
	private static String signAlgorithm="RS256";
	private static final String SSL = "SSL";
	public static String getJwt(byte[] data, PrivateKey privateKey, X509Certificate x509Certificate) {
		String jwsToken = null;
		JsonWebSignature jws = new JsonWebSignature();
		
		if(x509Certificate != null) {
			List<X509Certificate> certList = new ArrayList<>();
			certList.add(x509Certificate);
			X509Certificate[] certArray = certList.toArray(new X509Certificate[] {});
			jws.setCertificateChainHeaderValue(certArray);
		}
		
		jws.setPayloadBytes(data);
		jws.setAlgorithmHeaderValue(signAlgorithm);
		jws.setKey(privateKey);
		jws.setDoKeyValidation(false);
		try {
			jwsToken = jws.getCompactSerialization();
		} catch (JoseException e) {
			e.printStackTrace();
		}
		return jwsToken;

	}

	public static X509Certificate getCertificate() {
		
		try {
			FileInputStream certfis = new FileInputStream(

					new File(System.getProperty("user.dir") + "/files/keys/MosipTestCert.pem").getPath());

			String cert = getFileContent(certfis, "UTF-8");

			cert = trimBeginEnd(cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static PrivateKey getPrivateKey() {		
		try {
			FileInputStream pkeyfis = new FileInputStream(
					new File(System.getProperty("user.dir") + "/files/keys/PrivateKey.pem").getPath());

			String pKey = getFileContent(pkeyfis, "UTF-8");
			pKey = trimBeginEnd(pKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));
		} catch (Exception ex) {
			ex.printStackTrace();
			//throw new Exception("Failed to get private key");
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PublicKey getPublicKey()
			throws KeyManagementException, RestClientException, NoSuchAlgorithmException, InvalidKeySpecException {
		RestTemplate restTemplate = createTemplate();

		CryptomanagerRequestDto request = new CryptomanagerRequestDto();
		request.setApplicationId("IDA");
		//request.setData(org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8)));
		String publicKeyId = "IDA-FIR";
		request.setReferenceId(publicKeyId);
		String utcTime = getUTCCurrentDateTimeISOString();
		request.setTimeStamp(utcTime);
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put("appId", "IDA");

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString("https://qa.mosip.net/idauthentication/v1/internal/publickey/IDA")
				.queryParam("timeStamp", getUTCCurrentDateTimeISOString())
				.queryParam("referenceId", publicKeyId);
		ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
		String pKey= (String) ((Map<String, Object>) response.getBody().get("response")).get("publicKey");
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		return (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pKey)));
//		
		return KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(pKey)));
		
	}
	private static RestTemplate createTemplate() throws KeyManagementException, NoSuchAlgorithmException {
		turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String authToken = generateAuthToken();
				if (authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));
		return restTemplate;
	}
	public static String getUTCCurrentDateTimeISOString() {
		return DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
	}
	
	private static String generateAuthToken() {
		ObjectNode requestBody = mapper.createObjectNode();

		//TODO check client id and secret key
		requestBody.put("clientId", "mosip-regproc-client");
		requestBody.put("secretKey", "abc123");

		//TODO check app id
		requestBody.put("appId", "regproc");
		RequestWrapper<ObjectNode> request = new RequestWrapper<>();
		request.setRequesttime(DateUtils.getUTCCurrentDateTime());
		request.setRequest(requestBody);
		ClientResponse response = WebClient
				.create("https://qa.mosip.net/v1/authmanager/authenticate/clientidsecretkey")
				.post().syncBody(request).exchange().block();
		List<ResponseCookie> list = response.cookies().get("Authorization");
		if (list != null && !list.isEmpty()) {
			ResponseCookie responseCookie = list.get(0);
			return responseCookie.getValue();
		}
		return "";
	}
	
	public static void turnOffSslChecking() throws KeyManagementException, java.security.NoSuchAlgorithmException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance(SSL);
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
	
	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)
				throws CertificateException {
		}
	} };

//	public static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//
//		FileInputStream pkeyfis = new FileInputStream(
//				new File(System.getProperty("user.dir") + "/files/keys/new.pem").getPath());
//		String pKey = getFileContent(pkeyfis, "UTF-8");
//		pKey = trimBeginEnd(pKey);
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//
//		return (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pKey)));
//
//	}

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

	private static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

}
