package io.mosip.mock.sbi.devicehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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

	private static Map<String, PrivateKey> privateKeyMap = new ConcurrentHashMap<>();
	private static Map<String, Certificate> certificateMap = new ConcurrentHashMap<>();

	private String biometricImageType;
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
    private HashMap<String, Long> delayMap = new HashMap<> ();
    protected float qualityScore;
    protected boolean isQualityScoreSet;
    private boolean scoreFromIso = false;
    private SBICaptureInfo captureInfo;

	private String keystoreFilePath;
    
	public abstract long initDevice ();
	public abstract int deInitDevice ();
    public abstract int getLiveStream ();
    public abstract int getBioCapture (boolean isUsedForAuthenication) throws Exception;
			
	public SBIDeviceHelper(int port, String purpose, String deviceType, String deviceSubType, String keystoreFilePath, String biometricImageType) {
		super();
		setKeystoreFilePath(keystoreFilePath);
		setPort(port);
		setPurpose (purpose);
		setDeviceType (deviceType);
		setDeviceSubType (deviceSubType);
		setDeviceStatus (SBIConstant.DEVICE_STATUS_ISREADY);
		setBiometricImageType (biometricImageType);
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
			switch (deviceType)
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DIGITALID_JSON);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DIGITALID_JSON);
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
						fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DIGITALID_JSON);
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DIGITALID_JSON);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DIGITALID_JSON);
							break;
					}
				break;
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
			switch (deviceType)
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DEVICEDEISCOVERYINFO_JSON);
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
						fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON);
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DEVICEDEISCOVERYINFO_JSON);
							break;
					}
				break;
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

					switch (getDeviceStatus())
					{						
						case SBIConstant.DEVICE_STATUS_NOTREADY:
							discoverDto.setError(new ErrorInfo ("110", SBIJsonInfo.getErrorDescription("en", "110"))); 
							break;
						case SBIConstant.DEVICE_STATUS_ISBUSY:
							discoverDto.setError(new ErrorInfo ("111", SBIJsonInfo.getErrorDescription("en", "111"))); 
							break;
						case SBIConstant.DEVICE_STATUS_NOTREGISTERED:
							discoverDto.setDeviceId("");
							discoverDto.setDeviceCode("");
							discoverDto.setPurpose("");
							discoverDto.setError(new ErrorInfo ("100", SBIJsonInfo.getErrorDescription("en", "100"))); 
							break;
						default:
							discoverDto.setError(new ErrorInfo ("0", SBIJsonInfo.getErrorDescription("en", "0"))); 
							break;
					}
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
		try {
			String purpose = getPurpose ();
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType)
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEINFO_JSON);
							keyStoreFileName = getKeystoreFilePath() + (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME));
							keyAlias = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS));
							keyPwd = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD));
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DEVICEINFO_JSON);
							keyStoreFileName = getKeystoreFilePath() + (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME));
							keyAlias = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS));
							keyPwd = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD));
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					{
						fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEINFO_JSON);
						keyStoreFileName = getKeystoreFilePath() + (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME));
						keyAlias = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS));
						keyPwd = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD));
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON);
							keyStoreFileName = getKeystoreFilePath() + (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME));
							keyAlias = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS));
							keyPwd = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD));
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE:
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DEVICEINFO_JSON);
							keyStoreFileName = getKeystoreFilePath() + (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME));
							keyAlias = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS));
							keyPwd = (purpose.equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) ? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD_FTM) : ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD));
							break;
					}
				break;
			}

			deviceInfo = objectMapper.readValue(new File(fileName), DeviceInfo.class);
			if (deviceInfo != null)
			{
				deviceInfo.setDigitalId(getUnsignedDigitalId (digitalId, true));
				deviceInfo.setDeviceStatus(getDeviceStatus());
				deviceInfo.setPurpose(getPurpose ());
				deviceInfo.setCallbackId("http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort() + "/");
				if (!getDeviceStatus().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_NOTREGISTERED))
				{
					deviceInfo.setDigitalId(getSignedDigitalId (getUnsignedDigitalId (digitalId, false),
							getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
							getCertificate(keyStoreFileName, keyAlias, keyPwd)));
				}
				else
				{
					deviceInfo.setDeviceId("");
					deviceInfo.setDeviceCode("");
					deviceInfo.setPurpose("");
				}
			}
			return deviceInfo;
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfo :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}
	
	protected DeviceInfoDto getDeviceInfoDto(String deviceType, String deviceSubType, DeviceInfo deviceInfo) {
		DeviceInfoDto deviceInfoDto = new DeviceInfoDto ();
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType)
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD);
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					{
						keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
						keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
						keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD);
							break;
					}
				break;
			}

			String strDeviceInfo = objectMapper.writeValueAsString(deviceInfo);
			switch (getDeviceStatus())
			{
				case SBIConstant.DEVICE_STATUS_NOTREADY:
					deviceInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes("UTF-8"),
							getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
							(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
					deviceInfoDto.setError(new ErrorInfo ("110", SBIJsonInfo.getErrorDescription("en", "110")));
					break;
				case SBIConstant.DEVICE_STATUS_ISBUSY:
					deviceInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes("UTF-8"),
							getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
							(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
					deviceInfoDto.setError(new ErrorInfo ("111", SBIJsonInfo.getErrorDescription("en", "111")));
					break;
				case SBIConstant.DEVICE_STATUS_NOTREGISTERED:
					deviceInfoDto.setDeviceInfo(getUnsignedDeviceInfo (deviceInfo, true));
					deviceInfoDto.setError(new ErrorInfo ("100", SBIJsonInfo.getErrorDescription("en", "100")));
					break;
				default:
					deviceInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes("UTF-8"),
							getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
							(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
					deviceInfoDto.setError(new ErrorInfo ("0", SBIJsonInfo.getErrorDescription("en", "0")));
					break;
			}
			return deviceInfoDto ;
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfoDto :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}

	public String getSignBioMetricsDataDto(String deviceType, String deviceSubType, String currentBioData) {
		String signedBioMetricsDataDto = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		
		try {
			switch (deviceType)
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD);
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					{
						keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
						keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
						keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (deviceSubType)
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
							break;
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE:
							keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME);
							keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS);
							keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD);
							break;
					}
				break;
			}

			signedBioMetricsDataDto = JwtUtility.getJwt(currentBioData.getBytes("UTF-8"),
					getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
					(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd));
			return signedBioMetricsDataDto ;

		} catch (Exception ex) {
        	LOGGER.error("getSignBioMetricsDataDto :: deviceType::" + deviceType + " :: deviceSubType::" + deviceSubType , ex);
		}
		return null;
	}

	private String getUnsignedDeviceInfo (DeviceInfo deviceInfo, boolean isBase64URLEncoded)
    {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (isBase64URLEncoded)
			{
				return StringHelper.base64UrlEncode (objectMapper.writeValueAsString(deviceInfo));
			}
			else
			{
				return objectMapper.writeValueAsString(deviceInfo);
			}
		} catch (Exception ex) {
        	LOGGER.error("getUnsignedDeviceInfo :: " , ex);
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
		
	protected byte[] getLiveStreamBufferedImage() {
		byte[] image = null;
		String fileName = null;
		try {
			switch (getDeviceType())
			{
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
					switch (getDeviceSubType())
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP:
							switch (getDeviceSubId ())
							{
								case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_LEFT_HAND);
									break;
								case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_RIGHT_HAND);
									break;
								case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_THUMBS);
									break;
							}
							break;
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
					if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					{
						if (getDeviceSubId () == SBIConstant.DEVICE_FACE_SUB_TYPE_ID_FULLFACE)
							fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_SUBID_FULLFACE);
					}
				break;
				case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
					switch (getDeviceSubType())
					{
						case SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE:
							switch (getDeviceSubId ())
							{
								case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_LEFT:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_LEFT);
									break;
								case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_RIGHT:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_RIGHT);
									break;
								case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_BOTH:
									fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_BOTH);
									break;
							}
							break;
					}
				break;
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

	protected byte[] getBiometricISOImage(String seedName, String bioSubTypeFileName) {
		byte[] image = null;
		String fileName = null;
		boolean isFolderExist = true;
		try {
			fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PROFILE_FOLDER_PATH) + File.separator + getProfileId () + File.separator + getPurpose();
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
				fileName = fileName + File.separator + seedName + bioSubTypeFileName;
	            LOGGER.info("getBiometricISOImage :: bioSubTypeFileName :: " + bioSubTypeFileName + " :: fileName ::" + fileName);
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
	
	public String getBiometricImageType() {
		return biometricImageType;
	}

	public void setBiometricImageType(String biometricImageType) {
		this.biometricImageType = biometricImageType;
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

	public String getKeystoreFilePath() {
		return keystoreFilePath;
	}
	public void setKeystoreFilePath(String keystoreFilePath) {
		try {
			this.keystoreFilePath = keystoreFilePath != null ? keystoreFilePath : FileHelper.getCanonicalPath();
		} catch (IOException ex) {
			LOGGER.error("setKeystoreFilePath :: " + keystoreFilePath , ex);
		}
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
		String status = null;
		if (this.statusMap == null)
			this.statusMap = new HashMap<> ();

		if ((this.statusMap.containsKey(SBIConstant.DEVICE_STATUS) == false || SBIConstant.DEVICE_STATUS_ISREADY.equalsIgnoreCase (this.statusMap.get(SBIConstant.DEVICE_STATUS))))
        {
			status = SBIConstant.DEVICE_STATUS_ISREADY;;
            return status; 
        }
		switch (getDeviceStatus(SBIConstant.DEVICE_STATUS))
		{
			case SBIConstant.DEVICE_STATUS_ISBUSY:
				status = SBIConstant.DEVICE_STATUS_ISBUSY;
				break;
			case SBIConstant.DEVICE_STATUS_ISREADY:
				status = SBIConstant.DEVICE_STATUS_ISREADY;
				break;
			case SBIConstant.DEVICE_STATUS_NOTREGISTERED:
				status = SBIConstant.DEVICE_STATUS_NOTREGISTERED;
				break;
			default:
				status = SBIConstant.DEVICE_STATUS_NOTREADY;
				break;
			
		}
        return status;
	}

	public void setDeviceStatus(String deviceStatus) {
        if (this.statusMap.containsKey(SBIConstant.DEVICE_STATUS) == false)
        	this.statusMap.put(SBIConstant.DEVICE_STATUS, deviceStatus);
        else
        	this.statusMap.put(SBIConstant.DEVICE_STATUS, deviceStatus);
	}
	
	public String getDeviceStatus(String key) {
        if (this.statusMap.containsKey(key))
        	return this.statusMap.get(key);
        return null;
	}

	public boolean isScoreFromIso() {
		return scoreFromIso;
	}
	public void setScoreFromIso(boolean scoreFromIso) {
		this.scoreFromIso = scoreFromIso;
	}
	
	public long getDelayForMethod(String methodFor) {
		if (this.delayMap != null)
		{
	        if (this.delayMap.containsKey(methodFor))
	        	return this.delayMap.get(methodFor);
		}
		return 0;
	}

	public void setDelayForMethod(String[] methodFor, long delay) {
		if (this.delayMap == null)
			this.delayMap = new HashMap<> ();
		
		if (methodFor != null)
		{
			for (int index = 0; index < methodFor.length; index++) 
			{
				this.delayMap.putIfAbsent(methodFor[index], delay);
			}
		}
	}
	
	public void resetDelayForMethod()
	{
		if (this.delayMap != null)
		{
			this.delayMap.clear();
			this.delayMap = null;
		}
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
	
	public float getQualityScore() {
		if (this.qualityScore <= 0.0f || this.qualityScore > 100.0f)
			this.qualityScore = Float.parseFloat(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_MOCK_SBI_QUALITY_SCORE));
		
        return this.qualityScore;
	}

	public void setQualityScore(float qualityScore) {
        this.qualityScore = qualityScore;
	}

	public boolean isQualityScoreSet() {
		return isQualityScoreSet;
	}
	public void setQualityScoreSet(boolean isQualityScoreSet) {
		this.isQualityScoreSet = isQualityScoreSet;
	}		

	public int getRandomNumberForSeed(int seed) {
		int value = new Random().nextInt(seed);
		if (value == 0)
			value = 1;
		
		return value;
	}

	private PrivateKey getPrivateKey(String keyStoreFileName, String alias, String keystorePassword) {
		loadKeys(keyStoreFileName, alias, keystorePassword);
		return privateKeyMap.get(keyStoreFileName);
	}

	private Certificate getCertificate(String keyStoreFileName, String alias, String keystorePassword) {
		loadKeys(keyStoreFileName, alias, keystorePassword);
		return certificateMap.get(keyStoreFileName);
	}

	private void loadKeys(String keyStoreFileName, String alias, String keystorePassword) {
		if(privateKeyMap.containsKey(keyStoreFileName) && certificateMap.containsKey(keyStoreFileName)) {
			LOGGER.info("Keystore already cached, nothing to load :: " + keystoreFilePath);
			return;
		}

		try(FileInputStream fileInputStream = new FileInputStream(keyStoreFileName)) {
			LOGGER.info("Loading keystore into to local cache :: " + keystoreFilePath);
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(fileInputStream, keystorePassword.toCharArray());
			privateKeyMap.put(keyStoreFileName, (PrivateKey)keystore.getKey(alias, keystorePassword.toCharArray()));
			certificateMap.put(keyStoreFileName, keystore.getCertificate(alias));
		} catch (Exception e) {
			LOGGER.error("Failed to load keystore into local cache :: " + keystoreFilePath, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * To be invoked in afterSuite
	 * @param keystoreFilePath
	 * This method is not thread safe
	 */

	public static void evictKeys(String keystoreFilePath) {
		privateKeyMap.entrySet().removeIf(e -> e.getKey().startsWith(keystoreFilePath));
		certificateMap.entrySet().removeIf(e -> e.getKey().startsWith(keystoreFilePath));
	}
}
