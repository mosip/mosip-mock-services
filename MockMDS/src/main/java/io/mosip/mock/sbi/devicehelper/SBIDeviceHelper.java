package io.mosip.mock.sbi.devicehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.biometric.provider.CryptoUtility;
import org.biometric.provider.JwtUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.exception.SBIException;
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
	private static final Logger logger = LoggerFactory.getLogger(SBIDeviceHelper.class);

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
	private HashMap<String, String> statusMap = new HashMap<>();
	private HashMap<String, Long> delayMap = new HashMap<>();
	protected float qualityScore;
	protected boolean isQualityScoreSet;
	private boolean scoreFromIso = false;
	private SBICaptureInfo captureInfo;

	private String keystoreFilePath;

	public abstract long initDevice();

	public abstract int deInitDevice();

	public abstract int getLiveStream();

	@SuppressWarnings({ "java:S112" })
	public abstract int getBioCapture(boolean isUsedForAuthenication) throws Exception;

	private Random rand = new Random();

	protected SBIDeviceHelper(int port, String purpose, String deviceType, String deviceSubType,
			String keystoreFilePath, String biometricImageType) {
		super();
		setKeystoreFilePath(keystoreFilePath);
		setPort(port);
		setPurpose(purpose);
		setDeviceType(deviceType);
		setDeviceSubType(deviceSubType);
		setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
		setBiometricImageType(biometricImageType);
		initDeviceDetails();
	}

	public void initDeviceDetails() {
		setDigitalId(getDigitalId(getDeviceType(), getDeviceSubType()));
		setDiscoverDto(getDiscoverInfo(getDeviceType(), getDeviceSubType(), getDigitalId()));
		setDeviceInfo(getDeviceInfo(getDeviceType(), getDeviceSubType(), getDigitalId()));
		setDeviceInfoDto(getDeviceInfoDto(getDeviceType(), getDeviceSubType(), getDeviceInfo()));
	}

	protected DigitalId getDigitalId(String deviceType, String deviceSubType) {
		DigitalId digitalIdInfo = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DIGITALID_JSON);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DIGITALID_JSON);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					fileName = FileHelper.getCanonicalPath()
							+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DIGITALID_JSON);
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DIGITALID_JSON);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DIGITALID_JSON);
				}
				break;
			default:
				break;
			}

			if (FileHelper.exists(fileName)) {
				File file = new File(fileName);
				digitalIdInfo = objectMapper.readValue(file, DigitalId.class);
				if (digitalIdInfo != null) {
					digitalIdInfo.setDateTime(CryptoUtility.getTimestamp());
				}

				return digitalIdInfo;
			}
		} catch (Exception ex) {
			logger.info("getDigitalId :: deviceType:: {} :: deviceSubType:: {}", deviceType, deviceSubType);
			logger.error("getDigitalId :: error", ex);
		}
		return null;
	}

	@SuppressWarnings({ "java:S3776" })
	protected DiscoverDto getDiscoverInfo(String deviceType, String deviceSubType, DigitalId digitalId) {
		DiscoverDto discoverDtoInfo = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DEVICEDEISCOVERYINFO_JSON);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE))
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON);
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DEVICEDEISCOVERYINFO_JSON);
				}
				break;
			default:
				break;
			}

			if (FileHelper.exists(fileName)) {
				File jsonFile = new File(fileName);
				discoverDtoInfo = objectMapper.readValue(jsonFile, DiscoverDto.class);
				if (discoverDtoInfo != null) {
					discoverDtoInfo.setDigitalId(getUnsignedDigitalId(digitalId, true));
					discoverDtoInfo.setDeviceStatus(getDeviceStatus());
					discoverDtoInfo.setPurpose(getPurpose());
					discoverDtoInfo.setCallbackId(
							"http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":"
									+ getPort() + "/");
					switch (getDeviceStatus()) {
					case SBIConstant.DEVICE_STATUS_NOTREADY:
						discoverDtoInfo.setError(new ErrorInfo("110", SBIJsonInfo.getErrorDescription("en", "110")));
						break;
					case SBIConstant.DEVICE_STATUS_ISBUSY:
						discoverDtoInfo.setError(new ErrorInfo("111", SBIJsonInfo.getErrorDescription("en", "111")));
						break;
					case SBIConstant.DEVICE_STATUS_NOTREGISTERED:
						discoverDtoInfo.setDeviceId("");
						discoverDtoInfo.setDeviceCode("");
						discoverDtoInfo.setPurpose("");
						discoverDtoInfo.setError(new ErrorInfo("100", SBIJsonInfo.getErrorDescription("en", "100")));
						break;
					default:
						discoverDtoInfo.setError(new ErrorInfo("0", SBIJsonInfo.getErrorDescription("en", "0")));
						break;
					}
				}

				return discoverDtoInfo;
			}
		} catch (Exception ex) {
			logger.info("getDiscoverInfo :: deviceType:: {} :: deviceSubType:: {}", deviceType, deviceSubType);
			logger.error("getDiscoverInfo :: error", ex);
		}
		return null;
	}

	@SuppressWarnings({ "java:S3776", "java:S6541" })
	protected DeviceInfo getDeviceInfo(String deviceType, String deviceSubType, DigitalId digitalId) {
		DeviceInfo devInfo = null;
		String fileName = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEINFO_JSON);
					keyStoreFileName = getKeystoreFilePath() + (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME));
					keyAlias = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS));
					keyPwd = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD));
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SINGLE_DEVICEINFO_JSON);
					keyStoreFileName = getKeystoreFilePath() + (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME));
					keyAlias = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS));
					keyPwd = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD));
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)) {
					fileName = FileHelper.getCanonicalPath()
							+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEINFO_JSON);
					keyStoreFileName = getKeystoreFilePath() + (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME));
					keyAlias = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS_FTM)
							: ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS));
					keyPwd = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD));
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON);
					keyStoreFileName = getKeystoreFilePath() + (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME));
					keyAlias = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS));
					keyPwd = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD));
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_SINGLE_DEVICEINFO_JSON);
					keyStoreFileName = getKeystoreFilePath() + (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME));
					keyAlias = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS));
					keyPwd = (getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH)
							? ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD_FTM)
							: ApplicationPropertyHelper
									.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD));
				}
				break;
			default:
				break;
			}

			devInfo = objectMapper.readValue(new File(fileName), DeviceInfo.class);
			if (devInfo != null) {
				devInfo.setDigitalId(getUnsignedDigitalId(digitalId, true));
				devInfo.setDeviceStatus(getDeviceStatus());
				devInfo.setPurpose(getPurpose());
				devInfo.setCallbackId(
						"http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":"
								+ getPort() + "/");
				if (!getDeviceStatus().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_NOTREGISTERED)) {
					devInfo.setDigitalId(getSignedDigitalId(getUnsignedDigitalId(digitalId, false),
							getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
							getCertificate(keyStoreFileName, keyAlias, keyPwd)));
				} else {
					devInfo.setDeviceId("");
					devInfo.setDeviceCode("");
					devInfo.setPurpose("");
				}
			}
			return devInfo;
		} catch (Exception ex) {
			logger.info("getDeviceInfo :: deviceType:: {} :: deviceSubType:: {}", deviceType, deviceSubType);
			logger.error("getDeviceInfo :: error", ex);
		}
		return null;
	}

	protected DeviceInfoDto getDeviceInfoDto(String deviceType, String deviceSubType, DeviceInfo deviceInfo) {
		DeviceInfoDto devInfoDto = new DeviceInfoDto();
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			switch (deviceType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD);
					break;
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD);
				}
				break;
			default:
				break;
			}

			String strDeviceInfo = objectMapper.writeValueAsString(deviceInfo);
			switch (getDeviceStatus()) {
			case SBIConstant.DEVICE_STATUS_NOTREADY:
				devInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes(StandardCharsets.UTF_8),
						getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
						(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
				devInfoDto.setError(new ErrorInfo("110", SBIJsonInfo.getErrorDescription("en", "110")));
				break;
			case SBIConstant.DEVICE_STATUS_ISBUSY:
				devInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes(StandardCharsets.UTF_8),
						getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
						(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
				devInfoDto.setError(new ErrorInfo("111", SBIJsonInfo.getErrorDescription("en", "111")));
				break;
			case SBIConstant.DEVICE_STATUS_NOTREGISTERED:
				devInfoDto.setDeviceInfo(getUnsignedDeviceInfo(deviceInfo, true));
				devInfoDto.setError(new ErrorInfo("100", SBIJsonInfo.getErrorDescription("en", "100")));
				break;
			default:
				devInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes(StandardCharsets.UTF_8),
						getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
						(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd)));
				devInfoDto.setError(new ErrorInfo("0", SBIJsonInfo.getErrorDescription("en", "0")));
				break;
			}
			return devInfoDto;
		} catch (Exception ex) {
			logger.info("getDeviceInfoDto :: deviceType:: {} :: deviceSubType:: {}", deviceType, deviceSubType);
			logger.error("getDeviceInfoDto :: error", ex);
		}
		return null;
	}

	public String getSignBioMetricsDataDto(String deviceType, String deviceSubType, String currentBioData) {
		String signedBioMetricsDataDto = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;

		try {
			switch (deviceType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
				} else if (deviceSubType.equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
					keyStoreFileName = getKeystoreFilePath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME);
					keyAlias = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS);
					keyPwd = ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD);
				}
				break;
			default:
				break;
			}

			signedBioMetricsDataDto = JwtUtility.getJwt(currentBioData.getBytes(StandardCharsets.UTF_8),
					getPrivateKey(keyStoreFileName, keyAlias, keyPwd),
					(X509Certificate) getCertificate(keyStoreFileName, keyAlias, keyPwd));
			return signedBioMetricsDataDto;

		} catch (Exception ex) {
			logger.info("getSignBioMetricsDataDto :: deviceType:: {} :: deviceSubType:: {}", deviceType, deviceSubType);
			logger.error("getSignBioMetricsDataDto :: error", ex);
		}
		return null;
	}

	private String getUnsignedDeviceInfo(DeviceInfo deviceInfo, boolean isBase64URLEncoded) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (isBase64URLEncoded) {
				return StringHelper.base64UrlEncode(objectMapper.writeValueAsString(deviceInfo));
			} else {
				return objectMapper.writeValueAsString(deviceInfo);
			}
		} catch (Exception ex) {
			logger.error("getUnsignedDeviceInfo :: ", ex);
		}
		return null;
	}

	private String getUnsignedDigitalId(DigitalId digitalId, boolean isBase64URLEncoded) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (isBase64URLEncoded) {
				return StringHelper.base64UrlEncode(objectMapper.writeValueAsString(digitalId));
			} else {
				return objectMapper.writeValueAsString(digitalId);
			}
		} catch (Exception ex) {
			logger.error("getUnsignedDigitalId :: ", ex);
		}
		return null;
	}

	private String getSignedDigitalId(String digitalId, PrivateKey privateKey, Certificate cert) {
		try {
			return JwtUtility.getJwt(digitalId.getBytes(StandardCharsets.UTF_8), privateKey, (X509Certificate) cert);
		} catch (Exception ex) {
			logger.error("getSignedDigitalId :: ", ex);
		}
		return null;
	}

	@SuppressWarnings({ "java:S3776" })
	protected byte[] getLiveStreamBufferedImage() {
		byte[] image = null;
		String fileName = null;
		try {
			switch (getDeviceType()) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				if (getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
					switch (getDeviceSubId()) {
					case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_LEFT_HAND);
						break;
					case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_RIGHT_HAND);
						break;
					case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_SUBID_THUMBS);
						break;
					default:
						break;
					}
					break;
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				if (deviceSubType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)
						&& getDeviceSubId() == SBIConstant.DEVICE_FACE_SUB_TYPE_ID_FULLFACE) {
					fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
							.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_SUBID_FULLFACE);
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				if (getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
					switch (getDeviceSubId()) {
					case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_LEFT:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_LEFT);
						break;
					case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_RIGHT:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_RIGHT);
						break;
					case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_BOTH:
						fileName = FileHelper.getCanonicalPath() + ApplicationPropertyHelper
								.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_SUBID_BOTH);
						break;
					default:
						break;
					}
					break;
				}
				break;
			default:
				break;
			}

			if (FileHelper.exists(fileName)) {
				image = FileHelper.readAllBytes(fileName);
				return image;
			}
		} catch (Exception ex) {
			logger.info("getLiveStreamBufferedImage :: deviceType:: {} :: deviceSubType:: {}", deviceType,
					deviceSubType);
			logger.error("getLiveStreamBufferedImage :: error", ex);
		}
		return new byte[0];
	}

	protected byte[] getBiometricISOImage(String seedName, String bioSubTypeFileName) {
		byte[] image = null;
		String fileName = null;
		boolean isFolderExist = true;
		try {
			fileName = FileHelper.getCanonicalPath()
					+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PROFILE_FOLDER_PATH)
					+ File.separator + getProfileId() + File.separator + getPurpose();
			if (!FileHelper.directoryExists(fileName)) {
				isFolderExist = false;
				if (FileHelper.directoryExists(fileName)) {
					isFolderExist = true;
				}
			}
			if (isFolderExist) {
				fileName = fileName + File.separator + seedName + bioSubTypeFileName;
				logger.info("getBiometricISOImage :: bioSubTypeFileName:: {} :: fileName:: {}", bioSubTypeFileName,
						fileName);
				if (FileHelper.exists(fileName)) {
					image = FileHelper.readAllBytes(fileName);
					return image;
				}
			}
		} catch (Exception ex) {
			logger.info("getBiometricISOImage :: profileId:: {} :: bioSubTypeFileName:: {}", getProfileId(),
					bioSubTypeFileName);
			logger.error("getBiometricISOImage :: error", ex);
		}
		return new byte[0];
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
			logger.error("setKeystoreFilePath :: " + keystoreFilePath, ex);
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
			this.statusMap = new HashMap<>();

		if ((!this.statusMap.containsKey(SBIConstant.DEVICE_STATUS)
				|| SBIConstant.DEVICE_STATUS_ISREADY.equalsIgnoreCase(this.statusMap.get(SBIConstant.DEVICE_STATUS)))) {
			status = SBIConstant.DEVICE_STATUS_ISREADY;
			return status;
		}
		switch (getDeviceStatus(SBIConstant.DEVICE_STATUS)) {
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

	@SuppressWarnings({ "java:S3923" })
	public void setDeviceStatus(String deviceStatus) {
		if (!this.statusMap.containsKey(SBIConstant.DEVICE_STATUS))
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
		if (this.delayMap != null && this.delayMap.containsKey(methodFor)) {
			return this.delayMap.get(methodFor);
		}
		return 0;
	}

	public void setDelayForMethod(String[] methodFor, long delay) {
		if (this.delayMap == null)
			this.delayMap = new HashMap<>();

		if (methodFor != null) {
			for (int index = 0; index < methodFor.length; index++) {
				this.delayMap.putIfAbsent(methodFor[index], delay);
			}
		}
	}

	public void resetDelayForMethod() {
		if (this.delayMap != null) {
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
			this.qualityScore = Float.parseFloat(
					ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_MOCK_SBI_QUALITY_SCORE));

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
		int value = rand.nextInt(seed);
		if (value == 0)
			value = 1;

		return value;
	}

	private PrivateKey getPrivateKey(String keyStoreFileName, String alias, String keystorePassword)
			throws SBIException {
		loadKeys(keyStoreFileName, alias, keystorePassword);
		return privateKeyMap.get(keyStoreFileName);
	}

	private Certificate getCertificate(String keyStoreFileName, String alias, String keystorePassword)
			throws SBIException {
		loadKeys(keyStoreFileName, alias, keystorePassword);
		return certificateMap.get(keyStoreFileName);
	}

	@SuppressWarnings({ "java:S2139" })
	private void loadKeys(String keyStoreFileName, String alias, String keystorePassword) throws SBIException {
		if (privateKeyMap.containsKey(keyStoreFileName) && certificateMap.containsKey(keyStoreFileName)) {
			logger.info("Keystore already cached, nothing to load :: {}", keystoreFilePath);
			return;
		}

		try (FileInputStream fileInputStream = new FileInputStream(keyStoreFileName)) {
			logger.info("Loading keystore into to local cache :: {}", keystoreFilePath);
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(fileInputStream, keystorePassword.toCharArray());
			privateKeyMap.put(keyStoreFileName, (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray()));
			certificateMap.put(keyStoreFileName, keystore.getCertificate(alias));
		} catch (Exception e) {
			logger.error("Failed to load keystore into local cache :: " + keystoreFilePath, e);
			throw new SBIException("loadKeys", "error", e);
		}
	}

	/**
	 * To be invoked in afterSuite
	 * 
	 * @param keystoreFilePath This method is not thread safe
	 */

	public static void evictKeys(String keystoreFilePath) {
		privateKeyMap.entrySet().removeIf(e -> e.getKey().startsWith(keystoreFilePath));
		certificateMap.entrySet().removeIf(e -> e.getKey().startsWith(keystoreFilePath));
	}
}