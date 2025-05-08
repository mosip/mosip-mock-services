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

/**
 * Utility class for cryptographic operations related to ABIS (Automated
 * Biometric Identification System) integration. Provides methods for decrypting
 * CBEFF (Common Biometric Exchange File Format) data using RSA and AES
 * algorithms. The class manages loading certificates and cryptographic keys
 * from a keystore defined in partner properties. Supports RSA decryption using
 * OAEP padding and AES decryption using GCM mode.
 *
 * <p>
 * <strong>Usage:</strong> This class is intended to be used within ABIS proxy
 * components to securely decrypt sensitive biometric data exchanged with
 * external systems.
 * </p>
 *
 * <p>
 * <strong>Key Methods:</strong>
 * <ul>
 * <li>{@link #decryptCbeff(String)}: Decrypts Base64-encoded CBEFF data using
 * the configured private key.</li>
 * <li>{@link #setCertificateValues(String, String, String, String)}: Sets
 * custom certificate values if not loaded from properties.</li>
 * </ul>
 *
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>Requires Spring Framework for environment configuration.</li>
 * <li>Uses Apache Commons Codec and SLF4J for logging.</li>
 * </ul>
 *
 * <p>
 * <strong>Security:</strong>
 * <ul>
 * <li>Uses RSA with OAEP padding for asymmetric decryption of symmetric
 * keys.</li>
 * <li>Uses AES in GCM mode for symmetric decryption of CBEFF data.</li>
 * <li>Handles key material securely with strict exception handling to prevent
 * information leakage.</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> Instances of this class are thread-safe when
 * accessing shared environment properties.
 * </p>
 *
 * @version 1.0.0
 * @since 1.0.0
 */

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

	static final String KEY_SPLITTER = "#KEY_SPLITTER#";

	private static String certiPassword;

	private static String alias;

	private static String keystore;

	private static String filePath;

	private static final int THUMBPRINT_LENGTH = 32;

	private static final int NONCE_SIZE = 12;

	private static final int AAD_SIZE = 32;

	protected static final byte[] VERSION_RSA_2048 = "VER_R2".getBytes();

	/**
	 * Loads certificate and keystore properties from the partner.properties file.
	 * Invoked once during initialization to set static configuration values.
	 */
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

	/**
	 * Decrypts the provided Base64-encoded CBEFF data using the configured private
	 * key.
	 *
	 * @param responseData Base64-encoded CBEFF data to decrypt
	 * @return Decrypted CBEFF data as a String
	 * @throws Exception if decryption fails due to invalid data or cryptographic
	 *                   errors
	 */
	@SuppressWarnings({ "java:S112" })
	public String decryptCbeff(String responseData) throws Exception {
		KeyStore.PrivateKeyEntry privateKey = getPrivateKeyEntryFromP12();
		byte[] responseBytes = Base64.decodeBase64(responseData);
		byte[] deryptedCbeffData = decryptCbeffData(responseBytes, privateKey);
		return new String(deryptedCbeffData);
	}

	/**
	 * Sets custom certificate and keystore values programmatically. Typically used
	 * for testing or alternate runtime configurations.
	 *
	 * @param filePathVal Path to the certificate file
	 * @param keystoreVal Keystore type (e.g., PKCS12)
	 * @param passwordVal Password for the certificate
	 * @param aliasVal    Alias of the private key entry in the keystore
	 */
	public static void setCertificateValues(String filePathVal, String keystoreVal, String passwordVal,
			String aliasVal) {
		alias = aliasVal;
		filePath = filePathVal;
		keystore = keystoreVal;
		certiPassword = passwordVal;
	}

	/**
	 * Retrieves the private key entry from the specified PKCS12 keystore using the
	 * configured alias and password.
	 *
	 * <p>
	 * This method loads the keystore from the file defined in the partner
	 * properties and retrieves the private key entry identified by the alias. If
	 * the certificate password is not set explicitly, it retrieves it from the
	 * properties file using {@link #setPropertyValues()}.
	 * </p>
	 *
	 * @return PrivateKeyEntry containing the private key and associated certificate
	 *         chain
	 * @throws KeyStoreException           if there is an issue accessing the
	 *                                     keystore
	 * @throws NoSuchAlgorithmException    if the algorithm required for keystore
	 *                                     type or key entry is not available
	 * @throws CertificateException        if there is an issue with the certificate
	 * @throws IOException                 if there is an I/O issue with reading the
	 *                                     keystore file
	 * @throws UnrecoverableEntryException if the private key cannot be recovered
	 *                                     from the keystore
	 */
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

	/**
	 * Decrypts the CBEFF (Common Biometric Exchange File Format) data using the
	 * provided private key entry.
	 *
	 * <p>
	 * This method decrypts the CBEFF data after parsing and verifying the
	 * encryption header. It handles decryption using asymmetric RSA for symmetric
	 * key decryption and AES in GCM mode for CBEFF data decryption.
	 * </p>
	 *
	 * @param responseData Base64-decoded byte array of the encrypted CBEFF data
	 * @param privateKey   PrivateKeyEntry containing the private key and associated
	 *                     certificate
	 * @return Decrypted CBEFF data as a byte array
	 * @throws AbisException if decryption fails due to invalid data or
	 *                       cryptographic errors
	 */
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
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Finds the index of the first occurrence of the specified key splitter
	 * sequence in the encrypted data array, starting from the given index.
	 *
	 * <p>
	 * This method iterates through the byte array {@code encryptedData} to locate
	 * the first byte matching {@code keySplitterFirstByte}. It then checks if the
	 * subsequent substring matches the {@code keySplitter} string. If found, it
	 * returns the index of the key splitter; otherwise, it continues iterating.
	 * </p>
	 *
	 * @param encryptedData     Byte array containing encrypted data where the
	 *                          splitter needs to be found
	 * @param keyDemiliterIndex Starting index within the encrypted data to begin
	 *                          searching for the splitter
	 * @param keySplitter       String representing the splitter sequence to locate
	 * @return Index of the first occurrence of the key splitter within the
	 *         encrypted data array
	 */
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

	/**
	 * Decrypts the encrypted random symmetric key using the provided RSA private
	 * key.
	 *
	 * <p>
	 * This method initializes a Cipher instance using RSA with OAEP padding and
	 * decrypts the encrypted random symmetric key {@code encRandomSymKey}. It
	 * expects the private key to be RSA and uses OAEP parameters for decryption.
	 * </p>
	 *
	 * @param privateKey      PrivateKey instance used for decrypting the encrypted
	 *                        random symmetric key
	 * @param encRandomSymKey Encrypted random symmetric key to be decrypted
	 * @return Decrypted random symmetric key as a byte array
	 * @throws IllegalBlockSizeException          if the block size of the
	 *                                            encryption is incorrect
	 * @throws BadPaddingException                if the padding of the encryption
	 *                                            is incorrect
	 * @throws NoSuchAlgorithmException           if RSA algorithm is not available
	 * @throws NoSuchPaddingException             if OAEP padding scheme is not
	 *                                            available
	 * @throws InvalidKeyException                if the private key is invalid for
	 *                                            decryption
	 * @throws InvalidAlgorithmParameterException if the OAEP parameters are invalid
	 */
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

	/**
	 * Decrypts the given data using the specified symmetric key using
	 * AES/GCM/NoPadding mode.
	 *
	 * <p>
	 * This method decrypts the provided {@code data} using AES/GCM/NoPadding mode
	 * with the given {@code key}. It expects the last {@code cipher.getBlockSize()}
	 * bytes of {@code data} to be the random IV used in encryption. The method
	 * initializes the Cipher instance with the specified key and GCM parameters,
	 * updates the Additional Authentication Data (AAD), and finally decrypts the
	 * data.
	 * </p>
	 *
	 * @param key  SecretKey instance representing the symmetric key used for
	 *             decryption
	 * @param data Encrypted data to be decrypted
	 * @return Decrypted data as a byte array
	 * @throws AbisException if decryption fails due to invalid parameters or
	 *                       cryptographic errors
	 */
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
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	/**
	 * Decrypts the given data using the specified symmetric key, nonce, and
	 * Additional Authentication Data (AAD) using AES/GCM/NoPadding mode.
	 *
	 * <p>
	 * This method decrypts the provided {@code data} using AES/GCM/NoPadding mode
	 * with the given {@code key}, {@code nonce}, and {@code aad}. It initializes
	 * the Cipher instance with the derived key specification, GCM parameters,
	 * updates the AAD, and decrypts the data.
	 * </p>
	 *
	 * @param key   SecretKey instance representing the symmetric key used for
	 *              decryption
	 * @param data  Encrypted data to be decrypted
	 * @param nonce Nonce value used during encryption
	 * @param aad   Additional Authentication Data used during encryption
	 * @return Decrypted data as a byte array
	 * @throws AbisException if decryption fails due to invalid parameters or
	 *                       cryptographic errors
	 */
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
			throw new AbisException(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	/**
	 * Computes the SHA-256 hash (thumbprint) of the encoded form of the given
	 * certificate.
	 *
	 * <p>
	 * This method calculates the SHA-256 hash of the encoded form of the provided
	 * {@code cert}. It utilizes DigestUtils from Apache Commons Codec to compute
	 * the hash.
	 * </p>
	 *
	 * @param cert Certificate whose thumbprint (SHA-256 hash) is to be computed
	 * @return SHA-256 hash of the certificate's encoded form as a byte array
	 * @throws CertificateEncodingException if an error occurs during the encoding
	 *                                      of the certificate
	 */
	@SuppressWarnings({ "java:S2139" })
	public byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		try {
			return DigestUtils.sha256(cert.getEncoded());
		} catch (CertificateEncodingException e) {
			logger.error("getCertificateThumbprint", e);
			throw e;
		}
	}

	/**
	 * Parses and retrieves the version header bytes from the encrypted key bytes.
	 *
	 * <p>
	 * This method extracts and returns the version header bytes from the beginning
	 * of the {@code encryptedKey} array. It compares the extracted bytes with
	 * {@code VERSION_RSA_2048} and returns an empty byte array if they do not
	 * match.
	 * </p>
	 *
	 * @param encryptedKey Encrypted key from which to extract the version header
	 *                     bytes
	 * @return Version header bytes if they match {@code VERSION_RSA_2048};
	 *         otherwise, an empty byte array
	 */
	private byte[] parseEncryptKeyHeader(byte[] encryptedKey) {
		byte[] versionHeaderBytes = Arrays.copyOfRange(encryptedKey, 0, VERSION_RSA_2048.length);
		if (!Arrays.equals(versionHeaderBytes, VERSION_RSA_2048))
			return new byte[0];
		return versionHeaderBytes;
	}
}