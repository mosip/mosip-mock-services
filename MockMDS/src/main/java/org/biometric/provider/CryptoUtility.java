package org.biometric.provider;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PSource.PSpecified;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtility {

	private static BouncyCastleProvider provider;
	private static final String ASYMMETRIC_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/PKCS5Padding";
	private static final int GCM_TAG_LENGTH = 128;
	private static final String RSA_ECB_NO_PADDING = "RSA/ECB/NoPadding";

	private static final int AES_KEY_LENGTH = 256;
	private static final String MGF1 = "MGF1";
	private static final String AES = "AES";
	private static final String HASH_ALGO = "SHA-256";
	private static final int ASYMMETRIC_KEY_LENGTH = 2048;

	private static Encoder urlSafeEncoder;

	/**
	 * Default UTC pattern.
	 */
	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	static {
		urlSafeEncoder = Base64.getUrlEncoder().withoutPadding();
		provider = init();
	}

	private static BouncyCastleProvider init() {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		return provider;
	}

	public static byte[] generateHash(byte[] message, String algorithm) {
		byte[] hash = null;
		try {
			// Registering the Bouncy Castle as the RSA provider.
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.reset();
			hash = digest.digest(message);
		} catch (GeneralSecurityException ex) {
			ex.printStackTrace();
		}
		return hash;
	}

	public static LocalDateTime getUTCCurrentDateTime() {
		return ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
	}
	
	public static String getTimestamp() {
		return formatToISOString(getUTCCurrentDateTime());
	}

	/**
	 * Formats java.time.LocalDateTime to UTC string in default ISO pattern -
	 * <b>yyyy-MM-dd'T'HH:mm:ss'Z'</b> ignoring zone offset.
	 * 
	 * @param localDateTime java.time.LocalDateTime
	 * 
	 * @return a date String
	 */

	public static String formatToISOString(LocalDateTime localDateTime) {
		return localDateTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}

	public static Map<String, String> encrypt(PublicKey publicKey, byte[] dataBytes, String transactionId) {

		Map<String, String> result = new HashMap<>();
		try {
			String timestamp = getTimestamp();
			byte[] xorResult = getXOR(timestamp, transactionId);

			byte[] aadBytes = getLastBytes(xorResult, 16);
			byte[] ivBytes = getLastBytes(xorResult, 12);

			SecretKey secretKey = getSymmetricKey();
			final byte[] encryptedData = symmetricEncrypt(secretKey, dataBytes, ivBytes, aadBytes);
			final byte[] encryptedSymmetricKey = asymmetricEncrypt(publicKey, secretKey.getEncoded());

			result.put("ENC_SESSION_KEY", encodeToURLSafeBase64(encryptedSymmetricKey));
			result.put("ENC_DATA", encodeToURLSafeBase64(encryptedData));
			result.put("TIMESTAMP", timestamp);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public static String decrypt(PrivateKey privateKey, String sessionKey, String data, String timestamp,
			String transactionId) {
		try {

			timestamp = timestamp.trim();
			byte[] xorResult = getXOR(timestamp, transactionId);
			byte[] aadBytes = getLastBytes(xorResult, 16);
			byte[] ivBytes = getLastBytes(xorResult, 12);

			byte[] decodedSessionKey = decodeURLSafeBase64(sessionKey);
			final byte[] symmetricKey = asymmetricDecrypt(privateKey, decodedSessionKey);
			SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");

			byte[] decodedData = decodeURLSafeBase64(data);
			final byte[] decryptedData = symmetricDecrypt(secretKeySpec, decodedData, ivBytes, aadBytes);
			return new String(decryptedData);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] symmetricDecrypt(SecretKeySpec secretKeySpec, byte[] dataBytes, byte[] ivBytes,
			byte[] aadBytes) {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
			cipher.updateAAD(aadBytes);
			return cipher.doFinal(dataBytes);
		} catch (Exception ex) {
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

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static SecretKey getSymmetricKey() throws NoSuchAlgorithmException {
		javax.crypto.KeyGenerator generator = KeyGenerator.getInstance(AES, provider);
		SecureRandom random = new SecureRandom();
		generator.init(AES_KEY_LENGTH, random);
		return generator.generateKey();
	}

	public static byte[] asymmetricEncrypt(PublicKey key, byte[] data) throws Exception {

		Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);

		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);
		return doFinal(data, cipher);
	}

	public static byte[] asymmetricDecrypt(PrivateKey key, byte[] data) throws Exception {

		Cipher cipher = Cipher.getInstance(RSA_ECB_NO_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, key);

		byte[] paddedPlainText = doFinal(data, cipher);
		if (paddedPlainText.length < ASYMMETRIC_KEY_LENGTH / 8) {
			byte[] tempPipe = new byte[ASYMMETRIC_KEY_LENGTH / 8];
			System.arraycopy(paddedPlainText, 0, tempPipe, tempPipe.length - paddedPlainText.length,
					paddedPlainText.length);
			paddedPlainText = tempPipe;
		}
		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		return unpadOEAPPadding(paddedPlainText, oaepParams);
	}

	private static byte[] unpadOEAPPadding(byte[] paddedPlainText, OAEPParameterSpec paramSpec) throws Exception {
		byte[] unpaddedData = null;
		// Generate an RSA key pair
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// Encrypt some data using the public key
		Cipher encryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
		encryptCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
		byte[] encryptedData = encryptCipher.doFinal(paddedPlainText);

		// Now, let's unpad the encrypted data
		unpaddedData = unpadOEAPPadding(encryptedData, (RSAPublicKey) keyPair.getPublic());

		// sun.security.rsa.RSAPadding padding =
		// sun.security.rsa.RSAPadding.getInstance(
		// sun.security.rsa.RSAPadding.PAD_OAEP_MGF1, ASYMMETRIC_KEY_LENGTH / 8, new
		// SecureRandom(), paramSpec);
		// unpaddedData = padding.unpad(paddedPlainText);
		return unpaddedData;
	}

	private static byte[] unpadOEAPPadding(byte[] paddedPlainText, RSAPublicKey publicKey) throws Exception {
		// Get modulus and public exponent from public key
		RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getPublicExponent());

		// Create OAEP parameters
		OAEPParameterSpec paramSpec = new OAEPParameterSpec(HASH_ALGO, // Hash algorithm
				MGF1, // MGF algorithm
				MGF1ParameterSpec.SHA256, // MGF1 parameter
				PSource.PSpecified.DEFAULT); // Source of encoding input

		// Create Cipher instance
		Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
		cipher.init(Cipher.UNWRAP_MODE, publicKey, paramSpec);

		// Unpad the padded plain text
		return cipher.doFinal(paddedPlainText);
	}

	private static byte[] doFinal(byte[] data, Cipher cipher) throws Exception {
		return cipher.doFinal(data);
	}

	// Function to insert n 0s in the
	// beginning of the given string
	static byte[] prependZeros(byte[] str, int n) {
		byte[] newBytes = new byte[str.length + n];
		int i = 0;
		for (; i < n; i++) {
			newBytes[i] = 0;
		}

		for (int j = 0; i < newBytes.length; i++, j++) {
			newBytes[i] = str[j];
		}

		return newBytes;
	}

	// Function to return the XOR
	// of the given strings
	private static byte[] getXOR(String a, String b) {
		byte[] aBytes = a.getBytes();
		byte[] bBytes = b.getBytes();
		// Lengths of the given strings
		int aLen = aBytes.length;
		int bLen = bBytes.length;
		// Make both the strings of equal lengths
		// by inserting 0s in the beginning
		if (aLen > bLen) {
			bBytes = prependZeros(bBytes, aLen - bLen);
		} else if (bLen > aLen) {
			aBytes = prependZeros(aBytes, bLen - aLen);
		}
		// Updated length
		int len = Math.max(aLen, bLen);
		byte[] xorBytes = new byte[len];

		// To store the resultant XOR
		for (int i = 0; i < len; i++) {
			xorBytes[i] = (byte) (aBytes[i] ^ bBytes[i]);
		}
		return xorBytes;
	}

	private static byte[] getLastBytes(byte[] xorBytes, int lastBytesNum) {
		assert (xorBytes.length >= lastBytesNum);
		return java.util.Arrays.copyOfRange(xorBytes, xorBytes.length - lastBytesNum, xorBytes.length);
	}

	/**
	 * This method is used to check if the given <code>str</code> is null or an
	 * empty string.
	 * 
	 * @param str id of type java.lang.String
	 * @return true if given <code>str</code> is null or length of it is Zero after
	 *         trim.
	 */
	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	/**
	 * This method is used to check given <code>byte[]</code> is null or is Empty.
	 * 
	 * @param array of type byte.
	 * @return true if given <code>byte[]</code> is null or does not contains any
	 *         element inside it.
	 */
	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static String encodeToURLSafeBase64(byte[] data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data);
	}

	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return Base64.getUrlDecoder().decode(data);
	}

	public static void main(String[] args) throws Exception {
		String data = "this is my test";

		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair pair = gen.generateKeyPair();

		String timestamp = getTimestamp();
		String transactionId = "sdfsdf-sdfsd";
		byte[] xorResult = getXOR(timestamp, transactionId);
		byte[] aadBytes = getLastBytes(xorResult, 16);
		byte[] ivBytes = getLastBytes(xorResult, 12);
		byte[] dataBytes = data.getBytes();

		SecretKey secretKey = getSymmetricKey();
		final byte[] encryptedData = symmetricEncrypt(secretKey, dataBytes, ivBytes, aadBytes);
		final byte[] encryptedSymmetricKey = asymmetricEncrypt(pair.getPublic(), secretKey.getEncoded());

		String bioValue = encodeToURLSafeBase64(encryptedData);
		String sessionKey = encodeToURLSafeBase64(encryptedSymmetricKey);

		byte[] decodedSessionKey = decodeURLSafeBase64(sessionKey);
		final byte[] symmetricKey = asymmetricDecrypt(pair.getPrivate(), decodedSessionKey);
		SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");

		byte[] decodedBioValue = decodeURLSafeBase64(bioValue);
		final byte[] decryptedData = symmetricDecrypt(secretKeySpec, decodedBioValue, ivBytes, aadBytes);
		System.out.println(new String(decryptedData));
	}
}
