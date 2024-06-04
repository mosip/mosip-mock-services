package io.mosip.mock.sbi;

public class SBIConstant {
	private SBIConstant() {
		throw new IllegalStateException("SBIConstant class");
	}

	/** SBI Min Port Number. */
	public static final String MIN_PORT = "server.minport";
	/** SBI Max Port Number. */
	public static final String MAX_PORT = "server.maxport";

	/** SBI Server IP Address. */
	public static final String SERVER_ADDRESS = "server.serveripaddress";

	/** CORS HEADERS METHODS */
	public static final String CORS_HEADER_METHODS = "cors.headers.allowed.methods";

	/** SBI ALLOWED API CALLS */
	public static final String MOSIP_MOCK_SBI_ALLOWED_ADMIN_APIS = "mosip.mock.sbi.allowed.admin.apis";

	/** SBI BIOMETRIC DEFAULT QUALITY SCORE */
	public static final String MOSIP_MOCK_SBI_QUALITY_SCORE = "mosip.mock.sbi.quality.score";

	/** Environment */
	public static final String ENVIRONMENT_NONE = "None";
	public static final String ENVIRONMENT_STAGING = "Staging";
	public static final String ENVIRONMENT_DEVELOPER = "Developer";
	public static final String ENVIRONMENT_PRE_PRODUCTION = "Pre-Production";
	public static final String ENVIRONMENT_PRODUCTION = "Production";

	/** Purpose */
	public static final String PURPOSE_AUTH = "Auth";
	public static final String PURPOSE_REGISTRATION = "Registration";

	/** Device Status */
	public static final String DEVICE_STATUS = "DEVICE_STATUS";
	public static final String DEVICE_STATUS_ISREADY = "Ready";
	public static final String DEVICE_STATUS_ISBUSY = "Busy";
	public static final String DEVICE_STATUS_NOTREADY = "Not Ready";
	public static final String DEVICE_STATUS_NOTREGISTERED = "Not Registered";

	/** Device SubType Id Value */
	public static final int DEVICE_IRIS_SINGLE_SUB_TYPE_ID = 0; // Single IMAGE
	public static final int DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_LEFT = 1; // LEFT IRIS IMAGE
	public static final int DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_RIGHT = 2; // RIGHT IRIS IMAGE
	public static final int DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_BOTH = 3; // BOTH LEFT AND RIGHT IRIS IMAGE
	public static final int DEVICE_FINGER_SINGLE_SUB_TYPE_ID = 0; // Single IMAGE
	public static final int DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT = 1; // LEFT SLAP IMAGE
	public static final int DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT = 2; // RIGHT SLAP IMAGE
	public static final int DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB = 3;// TWO THUMB IMAGE
	public static final int DEVICE_FACE_SUB_TYPE_ID_FULLFACE = 0; // TWO THUMB IMAGE

	/** Bio Exceptions/Bio Subtype Names */
	public static final String BIO_NAME_UNKNOWN = "UNKNOWN";
	public static final String BIO_NAME_RIGHT_THUMB = "Right Thumb";
	public static final String BIO_NAME_RIGHT_INDEX = "Right IndexFinger";
	public static final String BIO_NAME_RIGHT_MIDDLE = "Right MiddleFinger";
	public static final String BIO_NAME_RIGHT_RING = "Right RingFinger";
	public static final String BIO_NAME_RIGHT_LITTLE = "Right LittleFinger";
	public static final String BIO_NAME_LEFT_THUMB = "Left Thumb";
	public static final String BIO_NAME_LEFT_INDEX = "Left IndexFinger";
	public static final String BIO_NAME_LEFT_MIDDLE = "Left MiddleFinger";
	public static final String BIO_NAME_LEFT_RING = "Left RingFinger";
	public static final String BIO_NAME_LEFT_LITTLE = "Left LittleFinger";
	public static final String BIO_NAME_RIGHT_IRIS = "Right";
	public static final String BIO_NAME_LEFT_IRIS = "Left";

	/** Profile Bio File Names */
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_THUMB = "Right_Thumb.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_INDEX = "Right_Index.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE = "Right_Middle.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_RING = "Right_Ring.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_LITTLE = "Right_Little.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_THUMB = "Left_Thumb.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_INDEX = "Left_Index.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_MIDDLE = "Left_Middle.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_RING = "Left_Ring.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_LITTLE = "Left_Little.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_THUMB_WSQ = "Right_Thumb_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_INDEX_WSQ = "Right_Index_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE_WSQ = "Right_Middle_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_RING_WSQ = "Right_Ring_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_RIGHT_LITTLE_WSQ = "Right_Little_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_THUMB_WSQ = "Left_Thumb_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_INDEX_WSQ = "Left_Index_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_MIDDLE_WSQ = "Left_Middle_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_RING_WSQ = "Left_Ring_wsq.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_LITTLE_WSQ = "Left_Little_wsq.iso";

	public static final String PROFILE_BIO_FILE_NAME_RIGHT_IRIS = "Right_Iris.iso";
	public static final String PROFILE_BIO_FILE_NAME_LEFT_IRIS = "Left_Iris.iso";
	public static final String PROFILE_BIO_FILE_NAME_FACE = "Face.iso";
	public static final String PROFILE_BIO_FILE_NAME_FACE_EXCEPTION = "Exception_Photo.iso";

	/** Profile Default. */
	public static final String PROFILE_DEFAULT = "Default";
	/** Profile Automatic. */
	public static final String PROFILE_AUTOMATIC = "Automatic";

	/** Errors */
	public static final String ERROR_CODE_0 = "mds_ERROR_0_msg_en";

	public static final String ERROR_CODE_100 = "mds_ERROR_100_msg_en";
	public static final String ERROR_CODE_101 = "mds_ERROR_101_msg_en";
	public static final String ERROR_CODE_102 = "mds_ERROR_102_msg_en";
	public static final String ERROR_CODE_103 = "mds_ERROR_103_msg_en";
	public static final String ERROR_CODE_104 = "mds_ERROR_104_msg_en";
	public static final String ERROR_CODE_105 = "mds_ERROR_105_msg_en";
	public static final String ERROR_CODE_106 = "mds_ERROR_106_msg_en";
	public static final String ERROR_CODE_107 = "mds_ERROR_107_msg_en";
	public static final String ERROR_CODE_108 = "mds_ERROR_108_msg_en";
	public static final String ERROR_CODE_109 = "mds_ERROR_109_msg_en";
	public static final String ERROR_CODE_110 = "mds_ERROR_110_msg_en";
	public static final String ERROR_CODE_111 = "mds_ERROR_111_msg_en";
	public static final String ERROR_CODE_112 = "mds_ERROR_112_msg_en";
	public static final String ERROR_CODE_113 = "mds_ERROR_113_msg_en";
	public static final String ERROR_CODE_114 = "mds_ERROR_114_msg_en";
	public static final String ERROR_CODE_115 = "mds_ERROR_115_msg_en";
	public static final String ERROR_CODE_116 = "mds_ERROR_116_msg_en";
	public static final String ERROR_CODE_117 = "mds_ERROR_117_msg_en";
	public static final String ERROR_CODE_118 = "mds_ERROR_118_msg_en";
	public static final String ERROR_CODE_119 = "mds_ERROR_119_msg_en";
	public static final String ERROR_CODE_120 = "mds_ERROR_120_msg_en";
	public static final String ERROR_CODE_121 = "mds_ERROR_121_msg_en";
	public static final String ERROR_CODE_122 = "mds_ERROR_122_msg_en";

	public static final String ERROR_CODE_500 = "mds_ERROR_500_msg_en";
	public static final String ERROR_CODE_501 = "mds_ERROR_501_msg_en";
	public static final String ERROR_CODE_502 = "mds_ERROR_502_msg_en";
	public static final String ERROR_CODE_503 = "mds_ERROR_503_msg_en";
	public static final String ERROR_CODE_504 = "mds_ERROR_504_msg_en";
	public static final String ERROR_CODE_505 = "mds_ERROR_505_msg_en";
	public static final String ERROR_CODE_506 = "mds_ERROR_506_msg_en";

	public static final String ERROR_CODE_551 = "mds_ERROR_551_msg_en";

	public static final String ERROR_CODE_601 = "mds_ERROR_601_msg_en";
	public static final String ERROR_CODE_604 = "mds_ERROR_604_msg_en";
	public static final String ERROR_CODE_605 = "mds_ERROR_605_msg_en";
	public static final String ERROR_CODE_606 = "mds_ERROR_606_msg_en";
	public static final String ERROR_CODE_607 = "mds_ERROR_607_msg_en";
	public static final String ERROR_CODE_608 = "mds_ERROR_608_msg_en";
	public static final String ERROR_CODE_609 = "mds_ERROR_609_msg_en";
	public static final String ERROR_CODE_610 = "mds_ERROR_610_msg_en";

	public static final String ERROR_CODE_700 = "mds_ERROR_700_msg_en";
	public static final String ERROR_CODE_701 = "mds_ERROR_701_msg_en";
	public static final String ERROR_CODE_702 = "mds_ERROR_702_msg_en";
	public static final String ERROR_CODE_703 = "mds_ERROR_703_msg_en";
	public static final String ERROR_CODE_704 = "mds_ERROR_704_msg_en";
	public static final String ERROR_CODE_705 = "mds_ERROR_705_msg_en";
	public static final String ERROR_CODE_706 = "mds_ERROR_706_msg_en";
	public static final String ERROR_CODE_707 = "mds_ERROR_707_msg_en";
	public static final String ERROR_CODE_708 = "mds_ERROR_708_msg_en";
	public static final String ERROR_CODE_709 = "mds_ERROR_709_msg_en";
	public static final String ERROR_CODE_710 = "mds_ERROR_710_msg_en";

	public static final String ERROR_CODE_800 = "mds_ERROR_800_msg_en";
	public static final String ERROR_CODE_801 = "mds_ERROR_801_msg_en";
	public static final String ERROR_CODE_803 = "mds_ERROR_803_msg_en";
	public static final String ERROR_CODE_804 = "mds_ERROR_804_msg_en";
	public static final String ERROR_CODE_805 = "mds_ERROR_805_msg_en";
	public static final String ERROR_CODE_806 = "mds_ERROR_806_msg_en";
	public static final String ERROR_CODE_809 = "mds_ERROR_809_msg_en";
	public static final String ERROR_CODE_810 = "mds_ERROR_810_msg_en";

	public static final String ERROR_CODE_999 = "mds_ERROR_999_msg_en";

	/** Mosip Methods */
	public static final String MOSIP_METHOD_MOSIPDISC = "MOSIPDISC";
	public static final String MOSIP_METHOD_MOSIPDINFO = "MOSIPDINFO";
	public static final String MOSIP_METHOD_CAPTURE = "CAPTURE";
	public static final String MOSIP_METHOD_STREAM = "STREAM";
	public static final String MOSIP_METHOD_RCAPTURE = "RCAPTURE";

	/** Mosip Verbs */
	public static final String MOSIP_POST_VERB = "POST / HTTP";
	public static final String MOSIP_GET_VERB = "GET / HTTP";
	public static final String MOSIP_DISC_VERB = "MOSIPDISC /device HTTP";
	public static final String MOSIP_INFO_VERB = "MOSIPDINFO /info HTTP";
	public static final String MOSIP_CAPTURE_VERB = "CAPTURE /capture HTTP";
	public static final String MOSIP_STREAM_VERB = "STREAM /stream HTTP";
	public static final String MOSIP_RCAPTURE_VERB = "RCAPTURE /capture HTTP";

	/** Mosip Admin Apis */
	public static final String MOSIP_ADMIN_API_STATUS = "POST /admin/status HTTP";
	public static final String MOSIP_ADMIN_API_SCORE = "POST /admin/score HTTP";
	public static final String MOSIP_ADMIN_API_DELAY = "POST /admin/delay HTTP";
	public static final String MOSIP_ADMIN_API_PROFILE = "POST /admin/profile HTTP";

	/** Command Line Arguments Names */
	public static final String MOSIP_PURPOSE = "mosip.mock.sbi.device.purpose";
	public static final String MOSIP_BIOMETRIC_TYPE = "mosip.mock.sbi.biometric.type";
	public static final String MOSIP_BIOMETRIC_IMAGE_TYPE = "mosip.mock.sbi.biometric.image.type";

	/** Device Purpose Names */
	public static final String MOSIP_PURPOSE_REGISTRATION = "Registration";
	public static final String MOSIP_PURPOSE_AUTH = "Auth";

	/** Biometric Types Names */
	public static final String MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE = "Biometric Device";
	public static final String MOSIP_BIOMETRIC_TYPE_FINGER = "Finger";
	public static final String MOSIP_BIOMETRIC_TYPE_FACE = "Face";
	public static final String MOSIP_BIOMETRIC_TYPE_IRIS = "Iris";

	/**
	 * Biometric Image Types Names For AUTH Finger [jp2000 or wsq][Iris and Face
	 * jp2000 only]
	 */
	public static final String MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000 = "JP2000";
	public static final String MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ = "WSQ";

	/** Biometric Sub Types Names */
	public static final String MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP = "Slap";
	public static final String MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE = "Single";
	public static final String MOSIP_BIOMETRIC_SUBTYPE_FINGER_TOUCHLESS = "Touchless";
	public static final String MOSIP_BIOMETRIC_SUBTYPE_FACE = "Full face";
	public static final String MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE = "Single";
	public static final String MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE = "Double";

	/** Biometric Auth seed Names */
	public static final String MOSIP_BIOMETRIC_AUTH_SEED_FINGER = "mosip.mock.sbi.biometric.auth.seed.finger";
	public static final String MOSIP_BIOMETRIC_AUTH_SEED_FACE = "mosip.mock.sbi.biometric.auth.seed.face";
	public static final String MOSIP_BIOMETRIC_AUTH_SEED_IRIS = "mosip.mock.sbi.biometric.auth.seed.iris";

	/** Biometric Registration seed Names */
	public static final String MOSIP_BIOMETRIC_REGISTRATION_SEED_FINGER = "mosip.mock.sbi.biometric.registration.seed.finger";
	public static final String MOSIP_BIOMETRIC_REGISTRATION_SEED_FACE = "mosip.mock.sbi.biometric.registration.seed.face";
	public static final String MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS = "mosip.mock.sbi.biometric.registration.seed.iris";

	/**
	 * Biometric Face DeviceDigitalId, DeviceDiscover, DeviceInfo Json and Stream
	 * Images, Device Keys, Mosip Public Key Info
	 */
	public static final String MOSIP_FACE_DIGITALID_JSON = "mosip.mock.sbi.file.face.digitalid.json";
	public static final String MOSIP_FACE_DEVICEINFO_JSON = "mosip.mock.sbi.file.face.deviceinfo.json";
	public static final String MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.face.devicediscovery.json";
	public static final String MOSIP_STREAM_FACE_SUBID_FULLFACE = "mosip.mock.sbi.file.face.streamimage";
	public static final String MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.face.keys.keystorefilename";
	public static final String MOSIP_STREAM_FACE_KEY_ALIAS = "mosip.mock.sbi.file.face.keys.keyalias";
	public static final String MOSIP_STREAM_FACE_KEYSTORE_PWD = "mosip.mock.sbi.file.face.keys.keystorepwd";
	public static final String MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME_FTM = "mosip.mock.sbi.file.face.keys.keystorefilename.ftm";
	public static final String MOSIP_STREAM_FACE_KEY_ALIAS_FTM = "mosip.mock.sbi.file.face.keys.keyalias.ftm";
	public static final String MOSIP_STREAM_FACE_KEYSTORE_PWD_FTM = "mosip.mock.sbi.file.face.keys.keystorepwd.ftm";

	/**
	 * Biometric Finger Slap DeviceDigitalId, DeviceDiscover, DeviceInfo Json and
	 * Stream Images, Device Keys, Mosip Public Key Info
	 */
	public static final String MOSIP_FINGER_SLAP_DIGITALID_JSON = "mosip.mock.sbi.file.finger.slap.digitalid.json";
	public static final String MOSIP_FINGER_SLAP_DEVICEINFO_JSON = "mosip.mock.sbi.file.finger.slap.deviceinfo.json";
	public static final String MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.finger.slap.devicediscovery.json";
	public static final String MOSIP_STREAM_FINGER_SLAP_SUBID_LEFT_HAND = "mosip.mock.sbi.file.finger.slap.streamimage.left";
	public static final String MOSIP_STREAM_FINGER_SLAP_SUBID_RIGHT_HAND = "mosip.mock.sbi.file.finger.slap.streamimage.right";
	public static final String MOSIP_STREAM_FINGER_SLAP_SUBID_THUMBS = "mosip.mock.sbi.file.finger.slap.streamimage.thumb";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.finger.slap.keys.keystorefilename";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS = "mosip.mock.sbi.file.finger.slap.keys.keyalias";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD = "mosip.mock.sbi.file.finger.slap.keys.keystorepwd";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME_FTM = "mosip.mock.sbi.file.finger.slap.keys.keystorefilename.ftm";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS_FTM = "mosip.mock.sbi.file.finger.slap.keys.keyalias.ftm";
	public static final String MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD_FTM = "mosip.mock.sbi.file.finger.slap.keys.keystorepwd.ftm";

	/**
	 * Biometric Finger Single/Auth only DeviceDigitalId, DeviceDiscover, DeviceInfo
	 * Json and Stream Images, Device Keys, Mosip Public Key Info
	 */
	public static final String MOSIP_FINGER_SINGLE_DIGITALID_JSON = "mosip.mock.sbi.file.finger.single.digitalid.json";
	public static final String MOSIP_FINGER_SINGLE_DEVICEINFO_JSON = "mosip.mock.sbi.file.finger.single.deviceinfo.json";
	public static final String MOSIP_FINGER_SINGLE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.finger.single.devicediscovery.json";
	public static final String MOSIP_STREAM_FINGER_SINGLE_SUBID_LEFT_HAND = "mosip.mock.sbi.file.finger.single.streamimage.left";
	public static final String MOSIP_STREAM_FINGER_SINGLE_SUBID_RIGHT_HAND = "mosip.mock.sbi.file.finger.single.streamimage.right";
	public static final String MOSIP_STREAM_FINGER_SINGLE_SUBID_THUMBS = "mosip.mock.sbi.file.finger.single.streamimage.thumb";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.finger.single.keys.keystorefilename";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS = "mosip.mock.sbi.file.finger.single.keys.keyalias";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD = "mosip.mock.sbi.file.finger.single.keys.keystorepwd";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_FILE_NAME_FTM = "mosip.mock.sbi.file.finger.single.keys.keystorefilename.ftm";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEY_ALIAS_FTM = "mosip.mock.sbi.file.finger.single.keys.keyalias.ftm";
	public static final String MOSIP_STREAM_FINGER_SINGLE_KEYSTORE_PWD_FTM = "mosip.mock.sbi.file.finger.single.keys.keystorepwd.ftm";

	/**
	 * Biometric Iris Double DeviceDigitalId, DeviceDiscover, DeviceInfo Json and
	 * Stream Images, Device Keys, Mosip Public Key Info
	 */
	public static final String MOSIP_IRIS_DOUBLE_DIGITALID_JSON = "mosip.mock.sbi.file.iris.double.digitalid.json";
	public static final String MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON = "mosip.mock.sbi.file.iris.double.deviceinfo.json";
	public static final String MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.iris.double.devicediscovery.json";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_SUBID_BOTH = "mosip.mock.sbi.file.iris.double.streamimage.both";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_SUBID_LEFT = "mosip.mock.sbi.file.iris.double.streamimage.left";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_SUBID_RIGHT = "mosip.mock.sbi.file.iris.double.streamimage.right";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.iris.double.keys.keystorefilename";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS = "mosip.mock.sbi.file.iris.double.keys.keyalias";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD = "mosip.mock.sbi.file.iris.double.keys.keystorepwd";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME_FTM = "mosip.mock.sbi.file.iris.double.keys.keystorefilename.ftm";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS_FTM = "mosip.mock.sbi.file.iris.double.keys.keyalias.ftm";
	public static final String MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD_FTM = "mosip.mock.sbi.file.iris.double.keys.keystorepwd.ftm";

	/**
	 * Biometric Iris Single/Auth only DeviceDigitalId, DeviceDiscover, DeviceInfo
	 * Json and Stream Images, Device Keys, Mosip Public Key Info
	 */
	public static final String MOSIP_IRIS_SINGLE_DIGITALID_JSON = "mosip.mock.sbi.file.iris.single.digitalid.json";
	public static final String MOSIP_IRIS_SINGLE_DEVICEINFO_JSON = "mosip.mock.sbi.file.iris.single.deviceinfo.json";
	public static final String MOSIP_IRIS_SINGLE_DEVICEDEISCOVERYINFO_JSON = "mosip.mock.sbi.file.iris.single.devicediscovery.json";
	public static final String MOSIP_STREAM_IRIS_SINGLE_SUBID_BOTH = "mosip.mock.sbi.file.iris.single.streamimage.both";
	public static final String MOSIP_STREAM_IRIS_SINGLE_SUBID_LEFT = "mosip.mock.sbi.file.iris.single.streamimage.left";
	public static final String MOSIP_STREAM_IRIS_SINGLE_SUBID_RIGHT = "mosip.mock.sbi.file.iris.single.streamimage.right";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME = "mosip.mock.sbi.file.iris.single.keys.keystorefilename";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS = "mosip.mock.sbi.file.iris.single.keys.keyalias";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD = "mosip.mock.sbi.file.iris.single.keys.keystorepwd";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_FILE_NAME_FTM = "mosip.mock.sbi.file.iris.single.keys.keystorefilename.ftm";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEY_ALIAS_FTM = "mosip.mock.sbi.file.iris.single.keys.keyalias.ftm";
	public static final String MOSIP_STREAM_IRIS_SINGLE_KEYSTORE_PWD_FTM = "mosip.mock.sbi.file.iris.single.keys.keystorepwd.ftm";

	public static final String MOSIP_PROFILE_FOLDER_PATH = "mosip.mock.sbi.folder.profile";
	public static final String MOSIP_PROFILE_DEFAULT_FOLDER_PATH = "mosip.mock.sbi.file.folder.default";
}