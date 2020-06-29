package org.biometric.provider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PSource.PSpecified;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class CryptoUtility {
	
	private static BouncyCastleProvider provider;
	private static final String asymmetricAlgorithm = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/PKCS5Padding";
	private static final int GCM_TAG_LENGTH = 128;
	private static final String RSA_ECB_NO_PADDING = "RSA/ECB/NoPadding";
	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";	
	private static final String MGF1 = "MGF1";
	private static final String HASH_ALGO = "SHA-256";
	private static final int asymmetricKeyLength = 2048;

	static {
		provider = init();
	}
	
	private static BouncyCastleProvider init() {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		return provider;
	}
	
	
	//TODO - cross check on which ISO format ?
	public static String getTimestamp() {
		LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
		return localDateTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}
	
	public static Map<String, String>  encrypt(PublicKey publicKey, String data) {
		Map<String, String> result = new HashMap<>();
		try {	
			
			LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
			String timestamp = localDateTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
			
			byte[] aadBytes = timestamp.substring(timestamp.length() - 16).getBytes();
			byte[] ivBytes = timestamp.substring(timestamp.length() - 12).getBytes();
			byte[] dataBytes = data.getBytes();
		
			SecretKey secretKey = getSymmetricKey();
			final byte[] encryptedData = symmetricEncrypt(secretKey, dataBytes, ivBytes, aadBytes);			
			final byte[] encryptedSymmetricKey =  asymmetricEncrypt(publicKey, secretKey.getEncoded());
					
			result.put("ENC_SESSION_KEY", java.util.Base64.getUrlEncoder().encodeToString(encryptedSymmetricKey));
			result.put("ENC_DATA", java.util.Base64.getUrlEncoder().encodeToString(encryptedData));
			result.put("TIMESTAMP", timestamp);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public static String decrypt(PrivateKey privateKey, String sessionKey, String data, String timestamp) {
		try {
			
			timestamp = timestamp.trim();
			byte[] aadBytes = timestamp.substring(timestamp.length() - 16).getBytes();
			byte[] ivBytes = timestamp.substring(timestamp.length() - 12).getBytes();
			
			byte[] decodedSessionKey =  java.util.Base64.getUrlDecoder().decode(sessionKey);		
			final byte[] symmetricKey = asymmetricDecrypt(privateKey, decodedSessionKey);		
			SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");
			
			byte[] decodedData =  java.util.Base64.getUrlDecoder().decode(data);
			final byte[] decryptedData = symmetricDecrypt(secretKeySpec, decodedData, ivBytes, aadBytes);
			return new String(decryptedData);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static byte[] symmetricDecrypt(SecretKeySpec secretKeySpec, byte[] dataBytes, byte[] ivBytes, byte[] aadBytes) {
		try {			
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);			
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
			cipher.updateAAD(aadBytes);
			return cipher.doFinal(dataBytes);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
	public static byte[] symmetricEncrypt(SecretKey secretKey, byte[] data, byte[] ivBytes, byte[] aadBytes) {
		try {			
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
			cipher.updateAAD(aadBytes);
			return cipher.doFinal(data);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
	public static SecretKey getSymmetricKey() throws NoSuchAlgorithmException {
		javax.crypto.KeyGenerator generator = KeyGenerator.getInstance("AES", provider);
		SecureRandom random = new SecureRandom();
		generator.init(256, random);
		return generator.generateKey();
	}
	
	public static byte[] asymmetricEncrypt(PublicKey key, byte[] data) throws Exception {
		
		Cipher cipher = Cipher.getInstance(asymmetricAlgorithm);
		
		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);
		return doFinal(data, cipher);
	}
 
	public static byte[] asymmetricDecrypt(PrivateKey key, byte[] data)  throws Exception {
	
		Cipher cipher = Cipher.getInstance(RSA_ECB_NO_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, key);
	
		byte[] paddedPlainText = doFinal(data, cipher);
		if (paddedPlainText.length < asymmetricKeyLength / 8) {
			byte[] tempPipe = new byte[asymmetricKeyLength / 8];
			System.arraycopy(paddedPlainText, 0, tempPipe, tempPipe.length - paddedPlainText.length,
					paddedPlainText.length);
			paddedPlainText = tempPipe;
		}
		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		return unpadOEAPPadding(paddedPlainText, oaepParams);
	}

	
	private static byte[] unpadOEAPPadding(byte[] paddedPlainText, OAEPParameterSpec paramSpec) throws Exception{
		byte[] unpaddedData = null;
		sun.security.rsa.RSAPadding padding = sun.security.rsa.RSAPadding.getInstance(
				sun.security.rsa.RSAPadding.PAD_OAEP_MGF1, asymmetricKeyLength / 8, new SecureRandom(), paramSpec);
		unpaddedData = padding.unpad(paddedPlainText);
		return unpaddedData;
	}
	
	private static byte[] doFinal(byte[] data, Cipher cipher) throws Exception {
		return cipher.doFinal(data);
	}
	
	public static void main(String[] args) throws Exception {
		String data = "this is my test";
		
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair pair = gen.generateKeyPair();
			
		LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
		String timestamp = localDateTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
		
		byte[] aadBytes = timestamp.substring(timestamp.length() - 16).getBytes();
		byte[] ivBytes = timestamp.substring(timestamp.length() - 12).getBytes();
		byte[] dataBytes = data.getBytes();
	
		SecretKey secretKey = getSymmetricKey();
		final byte[] encryptedData = symmetricEncrypt(secretKey, dataBytes, ivBytes, aadBytes);			
		final byte[] encryptedSymmetricKey =  asymmetricEncrypt(pair.getPublic(), secretKey.getEncoded());
		
		String bioValue = java.util.Base64.getUrlEncoder().encodeToString(encryptedData);
		String sessionKey = java.util.Base64.getUrlEncoder().encodeToString(encryptedSymmetricKey);
				
		byte[] decodedSessionKey =  java.util.Base64.getUrlDecoder().decode(sessionKey);		
		final byte[] symmetricKey = asymmetricDecrypt(pair.getPrivate(), decodedSessionKey);		
		SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");
		
		byte[] decodedBioValue =  java.util.Base64.getUrlDecoder().decode(bioValue);
		final byte[] decryptedData = symmetricDecrypt(secretKeySpec, decodedBioValue, ivBytes, aadBytes);
		System.out.println(new String(decryptedData));
	}
}
