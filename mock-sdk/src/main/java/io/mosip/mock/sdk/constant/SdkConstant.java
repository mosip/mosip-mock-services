package io.mosip.mock.sdk.constant;

/**
 * Constants used in the SDK configuration.
 * 
 * <p>
 * This class contains variables that represent keys used in the SDK
 * configuration files.
 * </p>
 * 
 * @author Janardhan B S
 */
public class SdkConstant {
	private SdkConstant() {
		throw new IllegalStateException("SdkConstant class");
	}

	/**
	 * SDK configuration key for ISO timestamp format used in checks.
	 * 
	 * <p>
	 * This constant represents the key used to retrieve the ISO timestamp format
	 * from SDK configurations.
	 * </p>
	 */
	public static final String SDK_CHECK_ISO_TIMESTAMP_FORMAT = "sdk_check_iso_timestamp_format";
}