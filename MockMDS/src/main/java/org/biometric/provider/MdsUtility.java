package org.biometric.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for cryptographic operations including asymmetric and symmetric
 * encryption.
 * <p>
 * This class provides methods to encrypt data using asymmetric and symmetric
 * algorithms, generate secure random initialization vectors (IVs), and obtain
 * symmetric keys.
 * </p>
 */
public class MdsUtility {
	private static final Logger logger = LoggerFactory.getLogger(MdsUtility.class);

	// @Value("${mosip.kernel.crypto.asymmetric-algorithm-name:RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING}")
	private static String asymmetricAlgorithm = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

	// @Value("${mosip.kernel.crypto.symmetric-algorithm-name:AES/GCM/PKCS5Padding}")
	private static String symmetricAlgorithm = "AES/GCM/PKCS5Padding";

	// @Value("${mosip.kernel.crypto.gcm-tag-length:128}")
	private static int tagLength = 128;

	private static final String HASH_ALGO = "SHA-256";

	private static final String MGF1 = "MGF1";

	private static final String AES = "AES";

	private static final int LENGTH_256 = 256;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private MdsUtility() {
		throw new IllegalStateException("MdsUtility class");
	}

	/**
	 * Encrypts data using an asymmetric algorithm.
	 *
	 * @param key  the public key used for encryption
	 * @param data the data to be encrypted
	 * @return the encrypted data
	 * @throws NoSuchAlgorithmException           if the specified algorithm is not
	 *                                            available
	 * @throws NoSuchPaddingException             if the specified padding mechanism
	 *                                            is not available
	 * @throws InvalidKeyException                if the key is invalid
	 * @throws InvalidAlgorithmParameterException if the algorithm parameters are
	 *                                            invalid
	 */
	public static byte[] asymmetricEncrypt(PublicKey key, byte[] data) throws java.security.NoSuchAlgorithmException,
			NoSuchPaddingException, java.security.InvalidKeyException, InvalidAlgorithmParameterException {

		Cipher cipher;
		cipher = Cipher.getInstance(asymmetricAlgorithm);

		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);

		cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);

		return doFinal(data, cipher);
	}

	/**
	 * Encrypts data using a symmetric algorithm.
	 *
	 * @param key  the secret key used for encryption
	 * @param data the data to be encrypted
	 * @param aad  additional authenticated data
	 * @return the encrypted data
	 * @throws InvalidKeyException                if the key is invalid
	 * @throws InvalidAlgorithmParameterException if the algorithm parameters are
	 *                                            invalid
	 * @throws NoSuchAlgorithmException           if the specified algorithm is not
	 *                                            available
	 * @throws NoSuchPaddingException             if the specified padding mechanism
	 *                                            is not available
	 */
	public static byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] aad) throws NullPointerException,
			InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(symmetricAlgorithm);

		byte[] output = null;
		byte[] randomIV = generateIV(cipher.getBlockSize());
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagLength, randomIV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
		output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
		if (aad != null && aad.length != 0) {
			cipher.updateAAD(aad);
		}
		byte[] processData = doFinal(data, cipher);
		Objects.requireNonNull(processData, SecurityExceptionCodeConstant.MOSIP_NULL_DATA_EXCEPTION.getErrorMessage());

		System.arraycopy(processData, 0, output, 0, processData.length);
		System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);

		return output;
	}

	/**
	 * Generates a random initialization vector (IV).
	 *
	 * @param blockSize the block size for the cipher
	 * @return the generated IV
	 */
	private static byte[] generateIV(int blockSize) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] byteIV = new byte[blockSize];
		secureRandom.nextBytes(byteIV);
		return byteIV;
	}

	/**
	 * Completes the encryption or decryption operation.
	 *
	 * @param data   the data to be processed
	 * @param cipher the cipher to be used
	 * @return the result of the operation
	 */
	private static byte[] doFinal(byte[] data, Cipher cipher) {
		byte[] dataResult = null;
		try {
			dataResult = cipher.doFinal(data);
		} catch (Exception e) {
			logger.info("doFinal", e);
		}
		return dataResult;
	}

	/**
	 * Generates a symmetric key.
	 *
	 * @return the generated symmetric key
	 * @throws Exception if an error occurs during key generation
	 */
	@SuppressWarnings({ "java:S112", "java:S2139" })
	public static SecretKey getSymmetricKey() throws Exception {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		javax.crypto.KeyGenerator generator = null;
		try {
			generator = javax.crypto.KeyGenerator.getInstance(AES, provider);
			SecureRandom random = new SecureRandom();
			generator.init(LENGTH_256, random);

			return generator.generateKey();
		} catch (Exception e) {
			logger.info("getSymmetricKey", e);
			throw e;
		}
	}

	/**
	 * Encrypts the biometric data with the timestamp and session key.
	 *
	 * @param bioValue   the biometric value to be encrypted
	 * @param timestamp  the timestamp used in the encryption
	 * @param sessionKey the session key used for encryption
	 * @return the encrypted data
	 * @throws InvalidKeyException                if the key is invalid
	 * @throws InvalidAlgorithmParameterException if the algorithm parameters are
	 *                                            invalid
	 * @throws NoSuchAlgorithmException           if the specified algorithm is not
	 *                                            available
	 * @throws NoSuchPaddingException             if the specified padding mechanism
	 *                                            is not available
	 */
	public static String encryptedData(String bioValue, String timestamp, SecretKey sessionKey)
			throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			NoSuchPaddingException {

		byte[] aad = Base64.getUrlEncoder().encode((timestamp.substring(timestamp.length() - 16).getBytes()));
		return Base64.getUrlEncoder().encodeToString(symmetricEncrypt(sessionKey, bioValue.getBytes(), aad));
	}
}