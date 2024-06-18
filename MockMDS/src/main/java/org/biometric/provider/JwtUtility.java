package org.biometric.provider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class JwtUtility {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtility.class);

	/** User Dir. */
	public static final String USER_DIR = "user.dir";

	// @Value("${mosip.kernel.crypto.sign-algorithm-name:RS256}")
	private static final String SIGN_ALGORITHM = "RS256";
	private static final String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	private static final String X509 = "X.509";

	/**
	 * Generates a JWT token using the provided data, private key, and optional
	 * X.509 certificate chain.
	 *
	 * @param data            the data to be included in the JWT payload
	 * @param privateKey      the private key used for signing the JWT
	 * @param x509Certificate the X.509 certificate chain to include in the JWT
	 *                        header (optional)
	 * @return the JWT token as a compact serialized string
	 */
	public static String getJwt(byte[] data, PrivateKey privateKey, X509Certificate x509Certificate) {
		String jwsToken = null;
		JsonWebSignature jws = new JsonWebSignature();

		if (x509Certificate != null) {
			List<X509Certificate> certList = new ArrayList<>();
			certList.add(x509Certificate);
			X509Certificate[] certArray = certList.toArray(new X509Certificate[] {});
			jws.setCertificateChainHeaderValue(certArray);
		}

		jws.setPayloadBytes(data);
		jws.setAlgorithmHeaderValue(SIGN_ALGORITHM);
		jws.setHeader(org.jose4j.jwx.HeaderParameterNames.TYPE, "JWT");
		jws.setKey(privateKey);
		jws.setDoKeyValidation(false);
		try {
			jwsToken = jws.getCompactSerialization();
		} catch (JoseException e) {
			logger.info("getJwt", e);
		}
		return jwsToken;

	}

	/**
	 * Retrieves an X.509 certificate from the local file system.
	 *
	 * @return the X.509 certificate object
	 */
	public static X509Certificate getCertificate() {
		try {
			FileInputStream certfis = new FileInputStream(

					new File(System.getProperty(USER_DIR) + "/files/keys/MosipTestCert.pem").getPath());

			String cert = getFileContent(certfis, StandardCharsets.UTF_8);

			cert = trimBeginEnd(cert);
			CertificateFactory cf = CertificateFactory.getInstance(X509);
			return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
		} catch (Exception ex) {
			logger.error("getCertificate", ex);
		}
		return null;
	}

	/**
	 * Retrieves a private key from the local file system.
	 *
	 * @return the PrivateKey object
	 */
	public static PrivateKey getPrivateKey() {
		try {
			FileInputStream pkeyfis = new FileInputStream(
					new File(System.getProperty(USER_DIR) + "/files/keys/PrivateKey.pem").getPath());

			String pKey = getFileContent(pkeyfis, StandardCharsets.UTF_8);
			pKey = trimBeginEnd(pKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pKey)));
		} catch (Exception ex) {
			logger.error("getPrivateKey", ex);
		}
		return null;
	}

	/**
	 * Retrieves a public key from the local file system.
	 *
	 * @return the PublicKey object
	 * @throws IOException              if an I/O error occurs
	 * @throws NoSuchAlgorithmException if the specified algorithm is not available
	 * @throws InvalidKeySpecException  if the provided key specification is invalid
	 */
	public static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		FileInputStream pkeyfis = new FileInputStream(
				new File(System.getProperty(USER_DIR) + "/files/keys/PublicKey.pem").getPath());
		String pKey = getFileContent(pkeyfis, StandardCharsets.UTF_8);
		pKey = trimBeginEnd(pKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		// PublicKey
		return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pKey)));
	}

	/**
	 * Retrieves the content of a file as a String.
	 *
	 * @param fis      the FileInputStream of the file
	 * @param encoding the character encoding of the file
	 * @return the content of the file as a String
	 * @throws IOException if an I/O error occurs
	 */
	public static String getFileContent(FileInputStream fis, Charset encoding) throws IOException {
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
	 * Retrieves the value of a property from the application configuration.
	 *
	 * @param key the key of the property to retrieve
	 * @return the value of the property
	 */
	public String getPropertyValue(String key) {
		return ApplicationPropertyHelper.getPropertyKeyValue(key);
	}

	/**
	 * Retrieves an X.509 certificate from the Identity and Authentication (IDA)
	 * server to be used for encrypting the biometric capture value.
	 * <p>
	 * The method communicates with the IDA server to fetch the certificate, decodes
	 * it from Base64 format, and returns the X509Certificate object.
	 * </p>
	 *
	 * @return the X509Certificate object used for encryption
	 * @throws Exception if an error occurs during the certificate retrieval or
	 *                   parsing
	 */
	public X509Certificate getCertificateToEncryptCaptureBioValue() throws Exception {
		String certificate = getCertificateFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance(X509);

		// X509Certificate
		return (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));
	}

	/**
	 * Computes the thumbprint (SHA-256 digest) of an X.509 certificate.
	 *
	 * @return the SHA-256 thumbprint of the X.509 certificate
	 * @throws Exception if an error occurs during the thumbprint computation
	 */
	public String getThumbprint() throws Exception {
		String certificate = getCertificateFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));

		// thumbprint
		return CryptoUtil.computeFingerPrint(x509Certificate.getEncoded(), null);
	}

	/**
	 * Computes the SHA-256 thumbprint of a given X.509 certificate.
	 *
	 * @param cert the X.509 certificate
	 * @return the SHA-256 thumbprint of the certificate
	 * @throws CertificateEncodingException if there is an error encoding the
	 *                                      certificate
	 */
	public static byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		return DigestUtils.sha256(cert.getEncoded());
	}

	/**
	 * Retrieves an X.509 certificate from a remote Identity and Authentication
	 * (IDA) server.
	 *
	 * @return the X.509 certificate object retrieved from the IDA server
	 * @throws Exception if an error occurs during the retrieval process
	 */
	@SuppressWarnings({ "java:S112", "java:S2139" })
	public String getCertificateFromIDA() throws Exception {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE, getPropertyValue("mosip.auth.appid"),
				getPropertyValue("mosip.auth.clientid"), getPropertyValue("mosip.auth.secretkey"),
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder().url(getPropertyValue("mosip.auth.server.url")).post(body).build();
		try {
			Response response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				String authToken = response.header("authorization");
				Request idarequest = new Request.Builder().header("cookie", "Authorization=" + authToken)
						.url(getPropertyValue("mosip.ida.server.url")).get().build();

				Response idaResponse = new OkHttpClient().newCall(idarequest).execute();
				if (idaResponse.isSuccessful()) {
					JSONObject jsonObject = new JSONObject(idaResponse.body().string());
					jsonObject = jsonObject.getJSONObject("response");
					return jsonObject.getString("certificate");
				}
			}
		} catch (IOException | JSONException e) {
			logger.error("getCertificateFromIDA", e);
			throw e;
		}
		return null;
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
	private static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}
}