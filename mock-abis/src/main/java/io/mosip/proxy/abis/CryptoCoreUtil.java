package io.mosip.proxy.abis;

import static java.util.Arrays.copyOfRange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
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

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javassist.bytecode.stackmap.TypeData.ClassName;

@Component
public class CryptoCoreUtil {

    private final static String RSA_ECB_OAEP_PADDING = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
 
    
    private static String certiPassword;
  
    private static String alias;
   
    private static String keystore;
  
    private static String filePath;

	public static void setPropertyValues() {
		Properties prop = new Properties();
		try {
			prop.load(CryptoCoreUtil.class.getClassLoader().getResourceAsStream("parter.properties"));
			certiPassword = prop.getProperty("cerificate.password");
			alias = prop.getProperty("cerificate.alias");
			keystore = prop.getProperty("certificate.keystore");
			filePath = prop.getProperty("certificate.filename");

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
    
	public String decrypt(String data) throws Exception {
		PrivateKey privateKey = loadP12();
		byte[] dataBytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
		byte[] data1 = decryptData(dataBytes, privateKey);
		String strData = new String(data1);
		return strData;
	}
	
	public static void setCertificateValues(String filePathVal, String keystoreVal, String passwordVal,
			String aliasVal) {
		alias = aliasVal;
		filePath = filePathVal;
		keystore = keystoreVal;
		certiPassword = passwordVal;

	}

	public static PrivateKey loadP12() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
			IOException, UnrecoverableEntryException {
		if (null == certiPassword || certiPassword.isEmpty()) {
			setPropertyValues();
		}
		KeyStore mosipKeyStore = KeyStore.getInstance(keystore);
		java.io.FileInputStream fis = new java.io.FileInputStream("src/main/resources/" + filePath);
		mosipKeyStore.load(fis, certiPassword.toCharArray());
		ProtectionParameter password = new PasswordProtection(certiPassword.toCharArray());
		PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) mosipKeyStore.getEntry(alias, password);
		PrivateKey privateKey = privateKeyEntry.getPrivateKey();

		return privateKey;
	}

	public static byte[] decryptData(byte[] key, PrivateKey privateKey) {

        String keySplitter = "#KEY_SPLITTER#";
        int keyDemiliterIndex = 0;
		SecretKey symmetricKey = null;
		byte[] encryptedData = null;
        final int cipherKeyandDataLength = key.length;
        final int keySplitterLength = keySplitter.length();
        keyDemiliterIndex = getSplitterIndex(key, keyDemiliterIndex, keySplitter);
        byte[] encryptedKey = copyOfRange(key, 0, keyDemiliterIndex);
		try {
			encryptedData = copyOfRange(key, keyDemiliterIndex + keySplitterLength, cipherKeyandDataLength);
        byte[] decryptedSymmetricKey = asymmetricDecrypt(privateKey, ((RSAPrivateKey) privateKey).getModulus(),
                encryptedKey);
			symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length, "AES");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return symmetricDecrypt(symmetricKey, encryptedData, null);
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
	 * @param keyModulus
	 * @param data
	 * @return
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	private static byte[] asymmetricDecrypt(PrivateKey privateKey, BigInteger keyModulus, byte[] data)
			throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InvalidKeyException {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ECB_OAEP_PADDING);
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                    PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
            return cipher.doFinal(data);
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

	/**
	 *
	 * @param paddedPlainText
	 * @param privateKey
	 * @return
	 * @throws InvalidCipherTextException
	 * @throws InvalidKeyException
	 */
	private static byte[] unpadOAEPPadding(byte[] paddedPlainText, BigInteger keyModulus)
			throws InvalidCipherTextException {

			OAEPEncoding encode = new OAEPEncoding(new RSAEngine(), new SHA256Digest());
			BigInteger exponent = new BigInteger("1");
			RSAKeyParameters keyParams = new RSAKeyParameters(false, keyModulus, exponent);
			encode.init(false, keyParams);
			return encode.processBlock(paddedPlainText, 0, paddedPlainText.length);
	}

	private static byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] aad) {
		byte[] output = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
			byte[] randomIV = Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length);
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, randomIV);

			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			if (aad != null && aad.length != 0) {
				cipher.updateAAD(aad);
			}
			output = cipher.doFinal(Arrays.copyOf(data, data.length - cipher.getBlockSize()));
		} catch (Exception e) {

	    }
		return output;
	} 
}

