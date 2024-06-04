package io.mosip.proxy.abis.utility;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.exception.AbisException;

@Component
@PropertySource({ "classpath:partner.properties" })
@SuppressWarnings("unused")
public class CryptoCoreUtil {
	private static final Logger logger = LoggerFactory.getLogger(CryptoCoreUtil.class);

	@SuppressWarnings({ "java:S6813" })
	@Autowired
	private Environment env;

	private static final String RSA_ECB_OAEP_PADDING = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
	/*
	 * Java 21
	 */
	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_TAG_LENGTH = 128;

	private static final String KEY_SPLITTER = "#KEY_SPLITTER#";

	private static String certiPassword;

	private static String alias;

	private static String keystore;

	private static String filePath;

	private static final int THUMBPRINT_LENGTH = 32;

	private static final int NONCE_SIZE = 12;

	private static final int AAD_SIZE = 32;

	protected static final byte[] VERSION_RSA_2048 = "VER_R2".getBytes();

	public static void setPropertyValues() {
		Properties prop = new Properties();
		try {
			prop.load(io.mosip.proxy.abis.utility.CryptoCoreUtil.class.getClassLoader()
					.getResourceAsStream("partner.properties"));
			certiPassword = prop.getProperty("certificate.password");
			alias = prop.getProperty("certificate.alias");
			keystore = prop.getProperty("certificate.keystore");
			filePath = prop.getProperty("certificate.filename");
		} catch (IOException e) {
			logger.error("setPropertyValues", e);
		}
	}

	@SuppressWarnings({ "java:S112" })
	public String decryptCbeff(String responseData) throws Exception {
		KeyStore.PrivateKeyEntry privateKey = getPrivateKeyEntryFromP12();
		byte[] responseBytes = Base64.decodeBase64(responseData);
		byte[] deryptedCbeffData = decryptCbeffData(responseBytes, privateKey);
		return new String(deryptedCbeffData);
	}

	public static void setCertificateValues(String filePathVal, String keystoreVal, String passwordVal,
			String aliasVal) {
		alias = aliasVal;
		filePath = filePathVal;
		keystore = keystoreVal;
		certiPassword = passwordVal;
	}

	private KeyStore.PrivateKeyEntry getPrivateKeyEntryFromP12() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableEntryException {
		if (null == certiPassword || certiPassword.isEmpty())
			setPropertyValues();
		KeyStore keyStore = KeyStore.getInstance(keystore);
		InputStream is = getClass().getResourceAsStream("/" + this.env.getProperty("certificate.filename"));
		keyStore.load(is, certiPassword.toCharArray());
		KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(certiPassword.toCharArray());
		return (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, password);
	}

	private byte[] decryptCbeffData(byte[] responseData, KeyStore.PrivateKeyEntry privateKey) throws AbisException {
		int cipherKeyandDataLength = responseData.length;
		int keySplitterLength = KEY_SPLITTER.length();
		int keyDemiliterIndex = getSplitterIndex(responseData, 0, KEY_SPLITTER);
		try {
			byte[] copiedBytes = Arrays.copyOfRange(responseData, 0, keyDemiliterIndex);
			byte[] encryptedCbeffData = Arrays.copyOfRange(responseData, keyDemiliterIndex + keySplitterLength,
					cipherKeyandDataLength);
			byte[] headerBytes = parseEncryptKeyHeader(copiedBytes);
			if (Arrays.equals(headerBytes, VERSION_RSA_2048)) {
				byte[] arrayOfByte1 = Arrays.copyOfRange(copiedBytes, 32 + VERSION_RSA_2048.length, copiedBytes.length);
				byte[] aad = Arrays.copyOfRange(encryptedCbeffData, 0, 32);
				byte[] nonce = Arrays.copyOfRange(aad, 0, 12);
				byte[] encCbeffData = Arrays.copyOfRange(encryptedCbeffData, 32, encryptedCbeffData.length);
				byte[] arrayOfByte2 = decryptRandomSymKey(privateKey.getPrivateKey(), arrayOfByte1);
				SecretKey secretKey = new SecretKeySpec(arrayOfByte2, 0, arrayOfByte2.length, "AES");
				return decryptCbeffData(secretKey, encCbeffData, nonce, aad);
			}
			byte[] dataCertThumbprint = null;
			byte[] encryptedSymmetricKey = null;
			if (copiedBytes.length > 256) {
				dataCertThumbprint = Arrays.copyOfRange(copiedBytes, 0, 32);
				encryptedSymmetricKey = Arrays.copyOfRange(copiedBytes, 32, copiedBytes.length);
			} else {
				encryptedSymmetricKey = copiedBytes;
			}
			byte[] certThumbprint = getCertificateThumbprint(privateKey.getCertificate());
			byte[] decryptedSymmetricKey = decryptRandomSymKey(privateKey.getPrivateKey(), encryptedSymmetricKey);
			SecretKey symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");
			return decryptCbeffData(symmetricKey, encryptedCbeffData);
		} catch (Exception e) {
			logger.error("decryptCbeffData with response", e);
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(), AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage());
		}
	}

	private static int getSplitterIndex(byte[] encryptedData, int keyDemiliterIndex, String keySplitter) {
		byte keySplitterFirstByte = keySplitter.getBytes()[0];
		int keySplitterLength = keySplitter.length();
		for (byte data : encryptedData) {
			if (data == keySplitterFirstByte) {
				String keySplit = new String(
						Arrays.copyOfRange(encryptedData, keyDemiliterIndex, keyDemiliterIndex + keySplitterLength));
				if (keySplitter.equals(keySplit))
					break;
			}
			keyDemiliterIndex++;
		}
		return keyDemiliterIndex;
	}

	private byte[] decryptRandomSymKey(PrivateKey privateKey, byte[] encRandomSymKey)
			throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance(RSA_ECB_OAEP_PADDING);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
					PSource.PSpecified.DEFAULT);
			cipher.init(2, privateKey, oaepParams);
			return cipher.doFinal(encRandomSymKey);
		} catch (NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException(e);
		} catch (NoSuchPaddingException e) {
			throw new NoSuchPaddingException(e.getMessage());
		} catch (InvalidKeyException e) {
			throw new InvalidKeyException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidAlgorithmParameterException(e);
		}
	}

	@SuppressWarnings({ "java:S2139" })
	private byte[] decryptCbeffData(SecretKey key, byte[] data) throws AbisException {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			byte[] randomIV = Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, randomIV);
			cipher.init(2, key, gcmParameterSpec);
			return cipher.doFinal(Arrays.copyOf(data, data.length - cipher.getBlockSize()));
		} catch (Exception e) {
			logger.error("decryptCbeffData", e);
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(), AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	@SuppressWarnings({ "java:S2139" })
	private byte[] decryptCbeffData(SecretKey key, byte[] data, byte[] nonce, byte[] aad) throws AbisException {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
			cipher.init(2, keySpec, gcmParameterSpec);
			cipher.updateAAD(aad);
			return cipher.doFinal(data, 0, data.length);
		} catch (Exception e) {
			logger.error("decryptCbeffData", e);
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(), AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	@SuppressWarnings({ "java:S2139" })
	public byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		try {
			return DigestUtils.sha256(cert.getEncoded());
		} catch (CertificateEncodingException e) {
			logger.error("getCertificateThumbprint", e);
			throw e;
		}
	}

	private byte[] parseEncryptKeyHeader(byte[] encryptedKey) {
		byte[] versionHeaderBytes = Arrays.copyOfRange(encryptedKey, 0, VERSION_RSA_2048.length);
		if (!Arrays.equals(versionHeaderBytes, VERSION_RSA_2048))
			return new byte[0];
		return versionHeaderBytes;
	}
}