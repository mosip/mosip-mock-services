package io.mosip.proxy.abis;

import static java.util.Arrays.copyOfRange;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
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
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CryptoCoreUtil {

	@Autowired
	private Environment env;

	private final static String RSA_ECB_OAEP_PADDING = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

	private static final String KEY_SPLITTER = "#KEY_SPLITTER#";

	private static String keystorePassword;

	private static String keystoreAlias;

	private static String keystoreType;

	private static String keystoreFilename;

	private final static int THUMBPRINT_LENGTH = 32;
	private final static int NONCE_SIZE = 12;
	private final static int AAD_SIZE = 32;
	public static final byte[] VERSION_RSA_2048 = "VER_R2".getBytes();

	private static String UPLOAD_FOLDER = System.getProperty("user.dir")+"/keystore";
	private static String PROPERTIES_FILE = UPLOAD_FOLDER+ "/partner.properties";

	public static void setPropertyValues() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(PROPERTIES_FILE));
			keystorePassword = prop.getProperty("keystore.password");
			keystoreAlias = prop.getProperty("keystore.alias");
			keystoreType = prop.getProperty("keystore.type");
			keystoreFilename = prop.getProperty("keystore.filename");

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public String decryptCbeff(String responseData) throws Exception {
		PrivateKeyEntry privateKey = getPrivateKeyEntryFromP12();
		byte[] responseBytes = org.apache.commons.codec.binary.Base64.decodeBase64(responseData);
		byte[] deryptedCbeffData = decryptCbeffData(responseBytes, privateKey);
		return new String(deryptedCbeffData);
	}

	public static void setCertificateValues(String filePathVal, String keystoreVal, String passwordVal,
											String aliasVal) {
		keystoreAlias = aliasVal;
		keystoreFilename = filePathVal;
		keystoreType = keystoreVal;
		keystorePassword = passwordVal;

	}

	private PrivateKeyEntry getPrivateKeyEntryFromP12() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException, UnrecoverableEntryException {
		if (null == keystorePassword || keystorePassword.isEmpty()) {
			setPropertyValues();
		}
		KeyStore keystoreInstance = KeyStore.getInstance(keystoreType);
		InputStream is = new FileInputStream(PROPERTIES_FILE);
		keystoreInstance.load(is, keystorePassword.toCharArray());
		ProtectionParameter password = new PasswordProtection(keystorePassword.toCharArray());
		PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) keystoreInstance.getEntry(keystoreAlias, password);

		return privateKeyEntry;
	}

	@SuppressWarnings("unused")
	private byte[] decryptCbeffData(byte[] responseData, PrivateKeyEntry privateKey) throws Exception {

		int cipherKeyandDataLength = responseData.length;
		int keySplitterLength = KEY_SPLITTER.length();
		int keyDemiliterIndex = getSplitterIndex(responseData, 0, KEY_SPLITTER);
		try {
			/*
				Copy the bytes from 0 location till KEY_SPLITTER index into an byte array. 
				Since 1.1.4 copied bytes will be Certificate Thumbprint + encrypted random symmetric key.
				Certificate thumbprint will be used as key/certificate identifier.
				Since 1.1.4 certificate thumbprint will be prepended to encrypted random symmetric key.
				Split the copied bytes from index 0 to 32 to get the certificate thumbprint.
				Split the copied bytes from index 32 to length of copied bytes to get the random symmetric key.
				Before 1.1.4 copied bytes does not prepended with certificate thumbprint, required to be used as encrypted random key.
			*/
			byte[] copiedBytes = copyOfRange(responseData, 0, keyDemiliterIndex);
			byte[] encryptedCbeffData = copyOfRange(responseData, keyDemiliterIndex + keySplitterLength, cipherKeyandDataLength);
			/*
				Added VERSION for the encrypted data along with AAD & Nonce for the AES-GCM encryption.
				VER_R2 is prepended to the encrypted random session key before the thumbprint.  
			*/
			byte[] headerBytes = parseEncryptKeyHeader(copiedBytes);
			if (Arrays.equals(headerBytes, VERSION_RSA_2048)) {
				byte[] encryptedSymmetricKey = Arrays.copyOfRange(copiedBytes, THUMBPRINT_LENGTH + VERSION_RSA_2048.length, copiedBytes.length);	
				byte[] aad = Arrays.copyOfRange(encryptedCbeffData, 0, AAD_SIZE);
				byte[] nonce = Arrays.copyOfRange(aad, 0, NONCE_SIZE);
				byte[] encCbeffData = Arrays.copyOfRange(encryptedCbeffData, AAD_SIZE, encryptedCbeffData.length);
				byte[] decryptedSymmetricKey = decryptRandomSymKey(privateKey.getPrivateKey(), encryptedSymmetricKey);
				SecretKey symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");
				return decryptCbeffData(symmetricKey, encCbeffData, nonce, aad);
			} 
			/*
				To Handle both 1.1.4 and before, check the size of copiedBytes.
				If copied bytes are more than 256, certificate is prepended to the encrypted random key 
				Otherwise copied bytes contains only encypted random key.
			*/
			byte[] dataCertThumbprint = null;
			byte[] encryptedSymmetricKey = null;
			if (copiedBytes.length > 256){
				dataCertThumbprint = Arrays.copyOfRange(copiedBytes, 0, THUMBPRINT_LENGTH);
				encryptedSymmetricKey = Arrays.copyOfRange(copiedBytes, THUMBPRINT_LENGTH, copiedBytes.length);
			} else {
				encryptedSymmetricKey = copiedBytes;
			}
			
			byte[] certThumbprint = getCertificateThumbprint(privateKey.getCertificate());
			/*
				Compare certificates thumbprint to verify certificate matches or not. 
				If does not match data will not get decrypted
			*/
			/*if (!Arrays.equals(dataThumbprint, certThumbprint)) {
				throw new CbeffException("Error in generating Certificate Thumbprint.");
			}*/

			byte[] decryptedSymmetricKey = decryptRandomSymKey(privateKey.getPrivateKey(), encryptedSymmetricKey);
			SecretKey symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");
			return decryptCbeffData(symmetricKey, encryptedCbeffData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new Exception("Error In Data Decryption.");
	}	

	private static int getSplitterIndex(byte[] encryptedData, int keyDemiliterIndex, String keySplitter) {
		final byte keySplitterFirstByte = keySplitter.getBytes()[0];
		final int keySplitterLength = keySplitter.length();
		for (byte data : encryptedData) {
			if (data == keySplitterFirstByte) {
				final String keySplit = new String(
						copyOfRange(encryptedData, keyDemiliterIndex, keyDemiliterIndex + keySplitterLength));
				if (keySplitter.equals(keySplit)) {
					break;
				}
			}
			keyDemiliterIndex++;
		}
		return keyDemiliterIndex;
	}

	/**
	 *
	 * @param privateKey
	 * @param encRandomSymKey
	 * @return
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	private byte[] decryptRandomSymKey(PrivateKey privateKey, byte[] encRandomSymKey)
			throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InvalidKeyException {

		try {
			Cipher cipher = Cipher.getInstance(RSA_ECB_OAEP_PADDING);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
					PSpecified.DEFAULT);
			cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
			return cipher.doFinal(encRandomSymKey);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException(e);
		} catch (NoSuchPaddingException e) {
			throw new NoSuchPaddingException(e.getMessage());
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidAlgorithmParameterException(e);
		}
	}

	private byte[] decryptCbeffData(SecretKey key, byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
			byte[] randomIV = Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, randomIV);
			cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
			return cipher.doFinal(Arrays.copyOf(data, data.length - cipher.getBlockSize()));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private byte[] decryptCbeffData(SecretKey key, byte[] data, byte[] nonce, byte[] aad)
			throws Exception {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			cipher.updateAAD(aad);
			
			return cipher.doFinal(data, 0, data.length);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		try {
			return DigestUtils.sha256(cert.getEncoded());
		} catch (CertificateEncodingException e) {
			throw e;
		}
	}

	private byte[] parseEncryptKeyHeader(byte[] encryptedKey) {
		byte[] versionHeaderBytes = Arrays.copyOfRange(encryptedKey, 0, VERSION_RSA_2048.length);
		if (!Arrays.equals(versionHeaderBytes, VERSION_RSA_2048)) {
			return new byte[0];
		}
		return versionHeaderBytes;
	}
}
