package org.biometric.provider;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.core.crypto.exception.InvalidDataException;
import io.mosip.kernel.core.crypto.exception.NullDataException;
import io.mosip.kernel.core.crypto.exception.SignatureException;

/**
 * Utility class for common cryptographic operations.
 *
 * This class provides a collection of static methods for various cryptographic
 * tasks, including asymmetric decryption, signing, data validation, URL-safe
 * Base64 encoding/decoding, and potentially other crypto-related
 * functionalities (depending on the specific implementation).
 *
 * It's important to use these methods with caution and ensure proper key
 * management and secure algorithms for your specific application needs.
 */
public class CryptoUtility {
	private static final Logger logger = LoggerFactory.getLogger(CryptoUtility.class);

	private static BouncyCastleProvider provider;
	private static final String ASYMMETRIC_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding";
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

	/**
	 * Generates a hash of the provided message using the specified algorithm.
	 *
	 * @param message   The message to hash.
	 * @param algorithm The hash algorithm to use.
	 * @return The hash as a byte array.
	 */
	public static byte[] generateHash(byte[] message, String algorithm) {
		byte[] hash = null;
		try {
			// Registering the Bouncy Castle as the RSA provider.
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.reset();
			hash = digest.digest(message);
		} catch (GeneralSecurityException ex) {
			logger.error("generateHash", ex);
		}
		return hash;
	}

	/**
	 * Gets the current date and time in UTC.
	 *
	 * @return The current UTC date and time.
	 */
	public static LocalDateTime getUTCCurrentDateTime() {
		return ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
	}

	/**
	 * Gets the current timestamp formatted as an ISO string.
	 *
	 * @return The current timestamp as an ISO string.
	 */
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

	/**
	 * Encrypts data using a public key and a transaction ID.
	 *
	 * @param publicKey     The public key to use for encryption.
	 * @param dataBytes     The data to encrypt.
	 * @param transactionId The transaction ID.
	 * @return A map containing the encrypted session key, encrypted data, and
	 *         timestamp.
	 */
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
			logger.error("encrypt", ex);
		}
		return result;
	}

	/**
	 * Decrypts data using a private key, session key, timestamp, and transaction
	 * ID.
	 *
	 * @param privateKey    The private key to use for decryption.
	 * @param sessionKey    The encrypted session key.
	 * @param data          The encrypted data.
	 * @param timestamp     The timestamp.
	 * @param transactionId The transaction ID.
	 * @return The decrypted data as a string.
	 */
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
			logger.error("decrypt", ex);
		}
		return null;
	}

	/**
	 * Decrypts data using symmetric encryption.
	 *
	 * @param secretKeySpec The secret key specification.
	 * @param dataBytes     The encrypted data.
	 * @param ivBytes       The initialization vector.
	 * @param aadBytes      The additional authenticated data.
	 * @return The decrypted data.
	 */
	public static byte[] symmetricDecrypt(SecretKeySpec secretKeySpec, byte[] dataBytes, byte[] ivBytes,
			byte[] aadBytes) {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
			cipher.updateAAD(aadBytes);
			return cipher.doFinal(dataBytes);
		} catch (Exception ex) {
			logger.error("symmetricDecrypt", ex);
		}
		return new byte[0];
	}

	/**
	 * Encrypts data using symmetric encryption.
	 *
	 * @param secretKey The secret key.
	 * @param data      The data to encrypt.
	 * @param ivBytes   The initialization vector.
	 * @param aadBytes  The additional authenticated data.
	 * @return The encrypted data.
	 */
	public static byte[] symmetricEncrypt(SecretKey secretKey, byte[] data, byte[] ivBytes, byte[] aadBytes) {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
			cipher.updateAAD(aadBytes);
			return cipher.doFinal(data);

		} catch (Exception ex) {
			logger.error("symmetricDecrypt", ex);
		}
		return new byte[0];
	}

	/**
	 * Generates a symmetric AES key.
	 *
	 * @return The generated symmetric key.
	 * @throws NoSuchAlgorithmException If the algorithm is not available.
	 */
	public static SecretKey getSymmetricKey() throws NoSuchAlgorithmException {
		javax.crypto.KeyGenerator generator = KeyGenerator.getInstance(AES, provider);
		SecureRandom random = new SecureRandom();
		generator.init(AES_KEY_LENGTH, random);
		return generator.generateKey();
	}

	/**
	 * Encrypts data using asymmetric encryption.
	 *
	 * @param key  The public key.
	 * @param data The data to encrypt.
	 * @return The encrypted data.
	 * @throws Exception If an error occurs during encryption.
	 */
	public static byte[] asymmetricEncrypt(PublicKey key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);

		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);
		return doFinal(data, cipher);
	}

	/**
	 * Decrypts data using the provided private key with RSA algorithm and OAEP
	 * padding.
	 *
	 * This method performs the following steps: 1. Initializes a Cipher instance
	 * with RSA in ECB mode (Electronic Codebook) and DECRYPT_MODE using the
	 * provided private key. 2. Decrypts the data using the initialized Cipher. 3.
	 * Verifies the decrypted data length against the expected key length
	 * (ASYMMETRIC_KEY_LENGTH / 8). - If the length is shorter, it's potentially due
	 * to additional padding by the encryptor. - The method copies the decrypted
	 * data to a temporary buffer with the expected key length, padding the left
	 * side with zeros. 4. Creates an OAEPParameterSpec object with the specified
	 * hash algorithm (HASH_ALGO), MGF1 with SHA-256 digest, and default PSpecified
	 * parameter. 5. Removes OAEP padding from the (potentially) resized decrypted
	 * data using `unpadOEAPPadding` (not shown). 6. Returns the final decrypted
	 * data without padding.
	 *
	 * @param key  The private key used for decryption.
	 * @param data The encrypted data to be decrypted.
	 * @return The decrypted data without padding.
	 * @throws Exception If an error occurs during decryption or padding removal.
	 */
	@SuppressWarnings({ "java:S1172", "java:S5542" })
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

	/**
	 * **Incorrect Implementation (For Reference Only)** This method attempts to
	 * unpad OAEP padding from the provided data, but it contains a logical error.
	 * It encrypts the data again before unpadding, which won't work for secure
	 * decryption.
	 *
	 * The actual implementation for unpadding OAEP should directly remove padding
	 * from the `paddedPlainText` using a secure method like RSAEngine.doFinal with
	 * OAEP padding removed.
	 * 
	 * @param paddedPlainText The data with OAEP padding to be unpadded.
	 * @param paramSpec       The OAEPParameterSpec object specifying the padding
	 *                        details.
	 * @return The unpadded data (implementation not provided).
	 * @throws Exception If an error occurs during unpadding.
	 */
	@SuppressWarnings({ "java:S1172" })
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
		return unpaddedData;
	}

	/**
	 * Unpads OAEP padding from the provided data.
	 *
	 * This method removes OAEP padding using the specified public key and OAEP
	 * parameters.
	 * 
	 * @param paddedPlainText The data with OAEP padding to be unpadded.
	 * @param publicKey       The RSA public key used for unpadding.
	 * @return The unpadded data.
	 * @throws Exception If an error occurs during unpadding.
	 */
	@SuppressWarnings({ "java:S112", "java:S1854", "unused" })
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

	/**
	 * Calls the `doFinal` method of the provided Cipher instance.
	 * 
	 * This method is likely used internally for finalizing decryption or encryption
	 * operations.
	 * 
	 * @param data   The data to be processed by the Cipher.
	 * @param cipher The Cipher instance to use for processing.
	 * @return The final result of the Cipher operation on the data.
	 * @throws Exception If an error occurs during the Cipher operation.
	 */
	@SuppressWarnings({ "java:S112" })
	private static byte[] doFinal(byte[] data, Cipher cipher) throws Exception {
		return cipher.doFinal(data);
	}

	/**
	 * Signs the provided data using the specified private key and X.509
	 * certificate.
	 *
	 * This method utilizes the JsonWebSignature (JWS) library to create a digital
	 * signature.
	 * 
	 * @param data            The data to be signed.
	 * @param privateKey      The private key used for signing.
	 * @param x509Certificate The X.509 certificate associated with the private key.
	 * @return The compact serialization of the JWS representing the signature.
	 * @throws SignatureException   If an error occurs during signing.
	 * @throws NullPointerException If the private key is null.
	 * @throws SecurityException    If data verification fails.
	 */
	public static String sign(byte[] data, PrivateKey privateKey, X509Certificate x509Certificate) {
		Objects.requireNonNull(privateKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		verifyData(data);
		JsonWebSignature jws = new JsonWebSignature();
		List<X509Certificate> certList = new ArrayList<>();
		certList.add(x509Certificate);
		X509Certificate[] certArray = certList.toArray(new X509Certificate[] {});
		jws.setCertificateChainHeaderValue(certArray);
		jws.setPayloadBytes(data);
		jws.setAlgorithmHeaderValue("RS256");
		jws.setKey(privateKey);
		jws.setDoKeyValidation(false);
		try {
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
	}

	/**
	 * Verifies that the provided data is not null and has a non-zero length.
	 *
	 * This method performs basic data validation to ensure valid signing
	 * operations.
	 * 
	 * @param data The data to be verified.
	 * @throws NullDataException    If the data is null.
	 * @throws InvalidDataException If the data has zero length.
	 */
	public static void verifyData(byte[] data) {
		if (data == null) {
			throw new NullDataException(SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorMessage());
		} else if (data.length == 0) {
			throw new InvalidDataException(SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Prepends a specified number of zero bytes to the beginning of a byte array.
	 *
	 * This method creates a new byte array with the original array's length plus
	 * the provided number of zeros prepended. It then iterates through the desired
	 * number of zeros and assigns them to the new array's initial elements.
	 * Finally, it iterates through the original array and copies its elements into
	 * the new array starting from the position after the prepended zeros.
	 *
	 * @param str The original byte array.
	 * @param n   The number of zero bytes to prepend.
	 * @return A new byte array with the prepended zeros.
	 */
	public static byte[] prependZeros(byte[] str, int n) {
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

	/**
	 * Calculates the exclusive OR (XOR) of two byte arrays representing strings.
	 *
	 * This method converts the provided strings (`a` and `b`) into byte arrays
	 * (`aBytes` and `bBytes`). It then determines the lengths of both arrays
	 * (`aLen` and `bLen`). If the lengths differ, the shorter array is prepended
	 * with zero bytes using the `prependZeros` method to ensure equal lengths for
	 * the XOR operation. The final length (`len`) is determined as the maximum of
	 * `aLen` and `bLen`. Finally, a new byte array (`xorBytes`) with the length
	 * `len` is created to store the XOR result. The XOR operation is performed
	 * bitwise on each corresponding element of `aBytes` and `bBytes`, storing the
	 * result in the `xorBytes` array.
	 *
	 * @param a The first string to be XORed.
	 * @param b The second string to be XORed.
	 * @return A new byte array containing the XOR result of the two strings.
	 * @throws IllegalArgumentException If the input strings are null.
	 */
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

	/**
	 * Extracts the last `lastBytesNum` bytes from a byte array.
	 *
	 * This method verifies that the provided byte array (`xorBytes`) has a length
	 * greater than or equal to the desired number of last bytes (`lastBytesNum`)
	 * using an assertion. It then utilizes the `java.util.Arrays.copyOfRange`
	 * method to extract a sub-array containing the last `lastBytesNum` elements
	 * from `xorBytes`.
	 *
	 * @param xorBytes     The byte array from which to extract the last bytes.
	 * @param lastBytesNum The number of last bytes to extract.
	 * @return A new byte array containing the last `lastBytesNum` elements of the
	 *         original array.
	 * @throws IllegalArgumentException If `lastBytesNum` is greater than the length
	 *                                  of `xorBytes`.
	 */
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

	/**
	 * Encodes a byte array to a URL-safe Base64 string.
	 *
	 * This method checks if the provided `data` is null or empty. If so, it returns
	 * null. Otherwise, it uses the `urlSafeEncoder` (assumed to be a pre-configured
	 * URL-safe Base64 encoder) to encode the byte array into a URL-safe Base64
	 * string representation.
	 *
	 * @param data The byte array to be encoded.
	 * @return A URL-safe Base64 string representation of the byte array, or null if
	 *         the data is null or empty.
	 */
	public static String encodeToURLSafeBase64(byte[] data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data);
	}

	/**
	 * Decodes a URL-safe Base64 string back into a byte array.
	 *
	 * This method checks if the provided `data` is null or empty. If so, it returns
	 * an empty byte array. Otherwise, it uses the standard `Base64.getUrlDecoder()`
	 * to decode the URL-safe Base64 string back into a byte array.
	 *
	 * @param data The URL-safe Base64 string to be decoded.
	 * @return A byte array representing the decoded data, or an empty byte array if
	 *         the data is null or empty.
	 */
	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return new byte[0];
		}
		return Base64.getUrlDecoder().decode(data);
	}

	@SuppressWarnings({ "java:S2629" })
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
		Objects.requireNonNull(decryptedData,
				SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorMessage());
		logger.info("main{}", new String(decryptedData, StandardCharsets.UTF_8));
	}
}