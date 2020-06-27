package org.biometric.provider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.List;

import org.bouncycastle.asn1.eac.RSAPublicKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;


public class JwtUtility {
	
	//TODO Need to be implement using properties 
	//@Value("${mosip.kernel.crypto.sign-algorithm-name:RS256}")
	private static String signAlgorithm="RS256";

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
	
	public static PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		FileInputStream pkeyfis = new FileInputStream(
				new File(System.getProperty("user.dir") + "/files/keys/PublicKey.pem").getPath());
		String pKey = getFileContent(pkeyfis, "UTF-8");
		pKey = trimBeginEnd(pKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		return (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pKey)));

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

	private static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

}
