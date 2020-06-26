package org.biometric.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MdsUtility {

	// TODO need to be implement using properties
	// @Value("${mosip.kernel.crypto.asymmetric-algorithm-name:RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING}")
	private static String asymmetricAlgorithm = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

	// TODO need to be implement using properties
	// @Value("${mosip.kernel.crypto.symmetric-algorithm-name:AES/GCM/PKCS5Padding}")
	private static String symmetricAlgorithm = "AES/GCM/PKCS5Padding";

	// TODO need to be implement using properties
	// @Value("${mosip.kernel.crypto.gcm-tag-length:128}")
	private static int tagLength = 128;

	// TODO need to be implement using properties
	private static final String HASH_ALGO = "SHA-256";

	// TODO need to be implement using properties
	private static final String MGF1 = "MGF1";

	// TODO need to be implement using properties
	private static final String AES = "AES";
	
	// TODO need to be implement using properties
	private static final String RSA ="RSA";
	
	// TODO need to be implement using properties
	private static int length_256=256;

	// Encrypted session key
	public static byte[] asymmetricEncrypt(PublicKey key, byte[] data) throws java.security.NoSuchAlgorithmException,
			NoSuchPaddingException, java.security.InvalidKeyException, InvalidAlgorithmParameterException {

		Cipher cipher;
		cipher = Cipher.getInstance(asymmetricAlgorithm);

		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);

		cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);

		return doFinal(data, cipher);
	}

	// encrypted data
	public static byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] aad) throws InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
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
		System.arraycopy(processData, 0, output, 0, processData.length);
		System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);

		return output;
	}

	private static byte[] generateIV(int blockSize) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] byteIV = new byte[blockSize];
		secureRandom.nextBytes(byteIV);
		return byteIV;
	}


	private static byte[] doFinal(byte[] data, Cipher cipher) {
		byte[] dataResult = null;
		try {
			dataResult = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataResult;
	}


	/**
	 * Gets the symmetric key.
	 *
	 * @return the symmetric key
	 */
	public static SecretKey getSymmetricKey() {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		javax.crypto.KeyGenerator generator = null;
		try {
			generator = javax.crypto.KeyGenerator.getInstance(AES, provider);
		} catch (java.security.NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		SecureRandom random = new SecureRandom();
		generator.init(length_256, random);

		return generator.generateKey();
	}

	public static String encryptedData(String bioValue, String timestamp,SecretKey sessionKey) throws InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {

		//TODO last 16 digits of the time stamp to be configurable
		byte[] aad = Base64.getUrlEncoder().encode((timestamp.substring(timestamp.length() - 16).getBytes()));
		return Base64.getUrlEncoder()
				.encodeToString(symmetricEncrypt(sessionKey, bioValue.getBytes(), aad));

	}

}
