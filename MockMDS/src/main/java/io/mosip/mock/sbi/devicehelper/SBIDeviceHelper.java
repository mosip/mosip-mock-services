package io.mosip.mock.sbi.devicehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.biometric.provider.CryptoUtility;
import org.biometric.provider.JwtUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.service.SBIJsonInfo;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.FileHelper;
import io.mosip.mock.sbi.util.StringHelper;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceInfoDto;
import io.mosip.registration.mdm.dto.DigitalId;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.ErrorInfo;

public abstract class SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIDeviceHelper.class);	

	private String purpose;
	private String profileId;
	private int port;
	private String deviceId;
	private int deviceSubId;

	private String deviceType;
	private String deviceSubType;

	private DigitalId digitalId;
	private DeviceInfo deviceInfo;
	private DiscoverDto discoverDto;
	private DeviceInfoDto deviceInfoDto;
    private HashMap<String, String> statusMap = new HashMap<> ();
    private SBICaptureInfo captureInfo;
    
	public abstract long initDevice ();
	public abstract int deInitDevice ();
    public abstract int getLiveStream ();
    public abstract int getBioCapture (boolean isUsedForAuthenication);
	
	public SBIDeviceHelper(int port, String purpose, String deviceType, String deviceSubType) {
		super();
		setPort(port);
		setPurpose (purpose);
		setDeviceType (deviceType);
		setDeviceSubType (deviceSubType);
		setDeviceStatus (SBIConstant.DEVICE_STATUS_ISREADY);
		initDeviceDetails();
	}

	public void initDeviceDetails() {
		setDigitalId (getDigitalId (getDeviceType (), getDeviceSubType ()));
		setDiscoverDto (getDiscoverInfo (getDeviceType (), getDeviceSubType (), getDigitalId ()));
		setDeviceInfo (getDeviceInfo (getDeviceType (), getDeviceSubType (), getDigitalId ()));
		setDeviceInfoDto (getDeviceInfoDto (getDeviceType (), getDeviceSubType (), getDeviceInfo ()));
	}

	protected DigitalId getDigitalId(String deviceType, String deviceSubType) {
		DigitalId digitalId = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DIGITALID_JSON);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DIGITALID_JSON);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DIGITALID_JSON);
			}
			if (FileHelper.exists(fileName)) 
			{
				File file = new File(fileName);
				digitalId = objectMapper.readValue(file, DigitalId.class);
				if (digitalId != null)
				{
					digitalId.setDateTime(CryptoUtility.getTimestamp());
				}
				
				return digitalId;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDigitalId :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}
	
	protected DiscoverDto getDiscoverInfo(String deviceType, String deviceSubType, DigitalId digitalId) {		
		DiscoverDto discoverDto = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON);
			}

			if (FileHelper.exists(fileName)) 
			{
				File jsonFile = new File(fileName);
				discoverDto = objectMapper.readValue(jsonFile, DiscoverDto.class);
				if (discoverDto != null)
				{
					discoverDto.setDigitalId(getUnsignedDigitalId (digitalId, true));
					discoverDto.setDeviceStatus(getDeviceStatus());
					discoverDto.setPurpose(getPurpose ());
					discoverDto.setCallbackId("http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort() + "/");
				}
				
				return discoverDto;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDiscoverInfo :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}
	
	protected DeviceInfo getDeviceInfo(String deviceType, String deviceSubType, DigitalId digitalId) {
		DeviceInfo deviceInfo = null;
		String fileName = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		FileInputStream inputStream = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
			}

			if (FileHelper.exists(fileName) && FileHelper.exists(keyStoreFileName)) 
			{
				File jsonFile = new File(fileName);
			    File keyStoreFile = new File(keyStoreFileName);
			    KeyStore keystore = null;
			    if (keyStoreFile.exists())
			    {
			    	inputStream = new FileInputStream (keyStoreFile);
					keystore = loadKeyStore (inputStream, keyPwd);			    	
			    }
				
				PrivateKey key = (PrivateKey)keystore.getKey(keyAlias, keyPwd.toCharArray());

	            /* Get certificate of public key */
	            java.security.cert.Certificate cert = keystore.getCertificate(keyAlias); 

	            /* Here it prints the public key*/
	            //LOGGER.Info("Public Key:");
	            //LOGGER.Info(cert.getPublicKey());

	            /* Here it prints the private key*/
	            //LOGGER.Info("\nPrivate Key:");
	            //LOGGER.Info(key);
	            
				deviceInfo = objectMapper.readValue(jsonFile, DeviceInfo.class);
				if (deviceInfo != null)
				{
					deviceInfo.setDigitalId(getUnsignedDigitalId (digitalId, false));
					deviceInfo.setDeviceStatus(getDeviceStatus());
					deviceInfo.setPurpose(getPurpose ());
					deviceInfo.setCallbackId("http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort() + "/");
					deviceInfo.setDigitalId(getSignedDigitalId (deviceInfo.getDigitalId(), key, cert));
				}
        		return deviceInfo;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfo :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		finally
		{
			try { // because close can throw an exception
		        if (inputStream != null) inputStream.close();
		    } catch (IOException ignored) {}
		}
		return null;
	}
	
	protected DeviceInfoDto getDeviceInfoDto(String deviceType, String deviceSubType, DeviceInfo deviceInfo) {
		DeviceInfoDto deviceInfoDto = new DeviceInfoDto ();
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		FileInputStream inputStream = null;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
			}

			if (FileHelper.exists(keyStoreFileName)) 
			{
				String strDeviceInfo = objectMapper.writeValueAsString(deviceInfo);
			    File keyStoreFile = new File(keyStoreFileName);
			    KeyStore keystore = null;
			    if (keyStoreFile.exists())
			    {
			    	inputStream = new FileInputStream (keyStoreFile);
					keystore = loadKeyStore (inputStream, keyPwd);			    	
			    }
								
				PrivateKey key = (PrivateKey)keystore.getKey(keyAlias, keyPwd.toCharArray());

	            /* Get certificate of public key */
	            java.security.cert.Certificate cert = keystore.getCertificate(keyAlias); 

	            /* Here it prints the public key*/
	            //LOGGER.Info("Public Key:");
	            //LOGGER.Info(cert.getPublicKey());

	            /* Here it prints the private key*/
	            //LOGGER.Info("\nPrivate Key:");
	            //LOGGER.Info(key);
	            deviceInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes("UTF-8"), key, (X509Certificate) cert));
           	 	deviceInfoDto.setError(new ErrorInfo ("100", SBIJsonInfo.getErrorDescription("en", "100"))); 
    		
        		return deviceInfoDto ;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfoDto :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		finally
		{
			try { // because close can throw an exception
		        if (inputStream != null) inputStream.close();
		    } catch (IOException ignored) {}
		}
		return null;
	}

	public String getSignBioMetricsDataDto(String deviceType, String deviceSubType, String currentBioData) {
		String signedBioMetricsDataDto = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		FileInputStream inputStream = null;
		
		try {
			if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
			}
			else if (deviceType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					deviceSubType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
			}

			if (FileHelper.exists(keyStoreFileName)) 
			{
				File keyStoreFile = new File(keyStoreFileName);
			    KeyStore keystore = null;
			    if (keyStoreFile.exists())
			    {
			    	inputStream = new FileInputStream (keyStoreFile);
					keystore = loadKeyStore (inputStream, keyPwd);			    	
			    }
				
				PrivateKey key = (PrivateKey)keystore.getKey(keyAlias, keyPwd.toCharArray());

	            /* Get certificate of public key */
	            java.security.cert.Certificate cert = keystore.getCertificate(keyAlias); 

	            /* Here it prints the public key*/
	            //LOGGER.Info("Public Key:");
	            //LOGGER.Info(cert.getPublicKey());

	            /* Here it prints the private key*/
	            //LOGGER.Info("\nPrivate Key:");
	            //LOGGER.Info(key);
	            signedBioMetricsDataDto = JwtUtility.getJwt(currentBioData.getBytes("UTF-8"), key, (X509Certificate) cert);
    		
        		return signedBioMetricsDataDto ;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getSignBioMetricsDataDto :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		finally
		{
			try { // because close can throw an exception
		        if (inputStream != null) inputStream.close();
		    } catch (IOException ignored) {}
		}		
		return null;
	}

	private String getUnsignedDigitalId (DigitalId digitalId, boolean isBase64URLEncoded)
    {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (isBase64URLEncoded)
			{
				return StringHelper.base64UrlEncode (objectMapper.writeValueAsString(digitalId));
			}
			else
			{
				return objectMapper.writeValueAsString(digitalId);
			}
		} catch (Exception ex) {
        	LOGGER.error("getUnsignedDigitalId :: " , ex);
		}
		return null;
    }

	private String getSignedDigitalId (String digitalId, PrivateKey privateKey, Certificate cert)
    {
		try {
			return JwtUtility.getJwt (digitalId.getBytes("UTF-8"), privateKey, (X509Certificate) cert);
		} catch (Exception ex) {
        	LOGGER.error("getSignedDigitalId :: " , ex);
		}
		return null;
    }
	
	private KeyStore loadKeyStore(FileInputStream inputStream, String keystorePwd) throws Exception {
	    KeyStore keyStore = KeyStore.getInstance("JKS");
        // if exists, load
        keyStore.load(inputStream, keystorePwd.toCharArray());

        /*
	    else {
	        // if not exists, create
	        keyStore.load(null, null);
	        keyStore.store(new FileOutputStream(file), keystorePwd.toCharArray());
	    }
	    */
	    return keyStore;
	}
		
	protected byte[] getLiveStreamBufferedImage() {
		byte[] image = null;
		String fileName = null;
		try {
			if (getDeviceType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
					getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
			{
				if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_LEFT_HAND);
				else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_RIGHT_HAND);
				else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_THUMBS);
			}
			else if (getDeviceType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
					getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
			{
				if (getDeviceSubId () == SBIConstant.DEVICE_FACE_SUB_TYPE_ID_FULLFACE)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_SUBID_FULLFACE);
			}
			else if (getDeviceType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
					getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
			{
				if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_LEFT);
				else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_RIGHT);
				else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_BOTH)
					fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_BOTH);
			}
			if (FileHelper.exists(fileName)) 
			{
				//image = FileHelper.readAllBytes ImageIO.read(new File(fileName));
				image = FileHelper.readAllBytes (fileName);
            	//LOGGER.info ("getLiveStreamBufferedImage :: fileName ::" + fileName);
				//LOGGER.info ("getLiveStreamBufferedImage :: image ::" + image.length);

				return image;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getLiveStreamBufferedImage :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}

	protected byte[] getBiometricISOImage(String bioSubTypeFileName) {
		byte[] image = null;
		String fileName = null;
		boolean isFolderExist = true;
		try {
			fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PROFILE_FOLDER_PATH) + File.separator + getProfileId ();
        	//LOGGER.error("getBiometricISOImage :: profileId::" + getProfileId() + " :: Not Set :: fileName :: " + fileName);
			if (!FileHelper.directoryExists(fileName))	
			{
				isFolderExist = false;
				if (FileHelper.directoryExists(fileName))	
				{
					isFolderExist = true;
				}				
			}
			if (isFolderExist)
			{
				fileName = fileName + File.separator + bioSubTypeFileName;
	            //LOGGER.info("getBiometricISOImage :: bioSubTypeFileName :: " + bioSubTypeFileName + " :: fileName ::" + fileName);
				if (FileHelper.exists(fileName))	
				{
					image = FileHelper.readAllBytes (fileName);
					return image;
				}				
			}
		} catch (Exception ex) {
        	LOGGER.error("getBiometricISOImage :: profileId::" + getProfileId() + " :: bioSubTypeFileName::" + bioSubTypeFileName, ex);
		}
		return null;
	}
	
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getProfileId() {
		return profileId;
	}
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceSubType() {
		return deviceSubType;
	}
	public void setDeviceSubType(String deviceSubType) {
		this.deviceSubType = deviceSubType;
	}
	public DigitalId getDigitalId() {
		return digitalId;
	}

	public void setDigitalId(DigitalId digitalId) {
		this.digitalId = digitalId;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public DiscoverDto getDiscoverDto() {
		return discoverDto;
	}
	public void setDiscoverDto(DiscoverDto discoverDto) {
		this.discoverDto = discoverDto;
	}
	public DeviceInfoDto getDeviceInfoDto() {
		return deviceInfoDto;
	}
	public void setDeviceInfoDto(DeviceInfoDto deviceInfoDto) {
		this.deviceInfoDto = deviceInfoDto;
	}
	public String getDeviceStatus() {
		if (statusMap == null)
            statusMap = new HashMap<> ();

		if ((this.statusMap.containsKey(SBIConstant.DEVICE_STATUS) == false || SBIConstant.DEVICE_STATUS_ISREADY.equals (this.statusMap.get(SBIConstant.DEVICE_STATUS))))
        {
            return SBIConstant.DEVICE_STATUS_ISREADY;
        }
        if (SBIConstant.DEVICE_STATUS_ISUSED.equals (this.statusMap.get(SBIConstant.DEVICE_STATUS)))
        {
            return SBIConstant.DEVICE_STATUS_ISUSED;
        }
        else
        {
            return SBIConstant.DEVICE_STATUS_NOTREADY;
        }
	}

	public void setDeviceStatus(String deviceStatus) {
        if (this.statusMap.containsKey(SBIConstant.DEVICE_STATUS) == false)
        	this.statusMap.put(SBIConstant.DEVICE_STATUS, deviceStatus);
        else
        	this.statusMap.put(SBIConstant.DEVICE_STATUS, deviceStatus);
	}
	
	public SBICaptureInfo getCaptureInfo() {
		return captureInfo;
	}
	public void setCaptureInfo(SBICaptureInfo captureInfo) {
		this.captureInfo = captureInfo;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public int getDeviceSubId() {
		return deviceSubId;
	}
	public void setDeviceSubId(int deviceSubId) {
		this.deviceSubId = deviceSubId;
	}	
}
