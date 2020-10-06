package io.mosip.mock.sbi;

public class SBIConstant {
	/** SBI Min Port Number. */
	public static String MIN_PORT = "server.minport";
	/** SBI Max Port Number. */
	public static String MAX_PORT = "server.maxport";

	/** SBI Server IP Address. */
	public static final String SERVER_ADDRESS = "server.serveripaddress";

	/** CORS HEADERS METHODS */
	public static final String CORS_HEADER_METHODS = "cors.headers.allowed.methods";
	
	/** Errors */
	public static final String Error_Code_100 = "mds_ERROR_100_msg_en";
	public static final String Error_Code_500 = "mds_ERROR_500_msg_en";
	public static final String Error_Code_501 = "mds_ERROR_501_msg_en";
	public static final String Error_Code_502 = "mds_ERROR_502_msg_en";
	public static final String Error_Code_503 = "mds_ERROR_503_msg_en";
	public static final String Error_Code_999 = "mds_ERROR_999_msg_en";
		
	/** Mosip Verbs */
	public static String MOSIP_POST_VERB = "POST / HTTP";
	public static String MOSIP_GET_VERB = "GET / HTTP";
	public static String MOSIP_DISC_VERB = "MOSIPDISC /device HTTP";
	public static String MOSIP_INFO_VERB = "MOSIPDINFO /info HTTP";
	public static String MOSIP_CAPTURE_VERB = "CAPTURE /capture HTTP";
	public static String MOSIP_STREAM_VERB = "STREAM /stream HTTP";
	public static String MOSIP_RCAPTURE_VERB = "RCAPTURE /capture HTTP";
	
	/** Biometric Types Names */
	public static String MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE = "mosip.mock.sbi.biometric.type.biometricdevice";
	public static String MOSIP_BIOMETRIC_TYPE_FINGER = "mosip.mock.sbi.biometric.type.finger";
	public static String MOSIP_BIOMETRIC_TYPE_FACE = "mosip.mock.sbi.biometric.type.face";
	public static String MOSIP_BIOMETRIC_TYPE_IRIS = "mosip.mock.sbi.biometric.type.iris";
			
	/** Biometric Face DeviceDigitalId, DeviceDiscover, DeviceInfo Json and Stream Images, Device Keys, Mosip Public Key Info */
	public static String MOSIP_FACE_DIGITALID_JSON = "mosip.mock.sbi.file.face.digitalid.json";
	public static String MOSIP_FACE_DEVICEINFO_JSON = "mosip.mock.sbi.file.face.deviceinfo.json";
	public static String MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.face.devicediscovery.json";
	public static String MOSIP_STREAM_FACE_SUBID_FULLFACE = "mosip.mock.sbi.file.face.streamimage";
	public static String MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.face.keys.keystorefilename";
	public static String MOSIP_STREAM_FACE_KEY_ALIAS = "mosip.mock.sbi.file.face.keys.keyalias";
	public static String MOSIP_STREAM_FACE_KEYSTORE_PWD = "mosip.mock.sbi.file.face.keys.keystorepwd";
	public static String MOSIP_STREAM_FACE_MOSIP_KEY = "mosip.mock.sbi.file.face.keys.encryption";
	
	/** Biometric Finger DeviceDigitalId, DeviceDiscover, DeviceInfo Json and Stream Images, Device Keys, Mosip Public Key Info */
	public static String MOSIP_FINGER_SLAP_DIGITALID_JSON = "mosip.mock.sbi.file.finger.slap.digitalid.json";
	public static String MOSIP_FINGER_SLAP_DEVICEINFO_JSON = "mosip.mock.sbi.file.finger.slap.deviceinfo.json";
	public static String MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.finger.slap.devicediscovery.json";
	public static String MOSIP_STREAM_FINGER_SLAP_SUBID_LEFT_HAND = "mosip.mock.sbi.file.finger.slap.streamimage.left";
	public static String MOSIP_STREAM_FINGER_SLAP_SUBID_RIGHT_HAND = "mosip.mock.sbi.file.finger.slap.streamimage.right";
	public static String MOSIP_STREAM_FINGER_SLAP_SUBID_THUMBS = "mosip.mock.sbi.file.finger.slap.streamimage.thumb";
	public static String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.finger.slap.keys.keystorefilename";
	public static String MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS = "mosip.mock.sbi.file.finger.slap.keys.keyalias";
	public static String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD = "mosip.mock.sbi.file.finger.slap.keys.keystorepwd";
	public static String MOSIP_STREAM_FINGER_SLAP_MOSIP_KEY = "mosip.mock.sbi.file.finger.slap.keys.encryption";
	
	/** Biometric Iris DeviceDigitalId, DeviceDiscover, DeviceInfo Json and Stream Images, Device Keys, Mosip Public Key Info */
	public static String MOSIP_IRIS_DOUBLE_DIGITALID_JSON = "mosip.mock.sbi.file.iris.double.digitalid.json";
	public static String MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON = "mosip.mock.sbi.file.iris.double.deviceinfo.json";
	public static String MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.iris.double.devicediscovery.json";
	public static String MOSIP_STREAM_IRIS_DOUBLE_SUBID_BOTH = "mosip.mock.sbi.file.iris.double.streamimage.both";
	public static String MOSIP_STREAM_IRIS_DOUBLE_SUBID_LEFT = "mosip.mock.sbi.file.iris.double.streamimage.left";
	public static String MOSIP_STREAM_IRIS_DOUBLE_SUBID_RIGHT = "mosip.mock.sbi.file.iris.double.streamimage.right";
	public static String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.iris.double.keys.keystorefilename";
	public static String MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS = "mosip.mock.sbi.file.iris.double.keys.keyalias";
	public static String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD = "mosip.mock.sbi.file.iris.double.keys.keystorepwd";
	public static String MOSIP_STREAM_IRIS_DOUBLE_MOSIP_KEY = "mosip.mock.sbi.file.iris.double.keys.encryption";	
}
