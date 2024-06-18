package io.mosip.proxy.abis.constant;

/**
 * Enum representing error codes and messages for ABIS (Automated Biometric
 * Identification System) operations. Each enum constant provides a specific
 * error code and corresponding error message to identify and handle exceptions.
 * 
 * <p>
 * The error codes follow a specific format "MOS-MABIS-XXX", where:
 * <ul>
 * <li>"MOS" indicates the organization or domain (Mosip).</li>
 * <li>"MABIS" specifies the specific module (Automated Biometric Identification
 * System).</li>
 * <li>"XXX" represents a unique numerical identifier for each error.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Each enum constant also provides methods to retrieve the error code and error
 * message, and a static method {@code fromErrorCode} to retrieve the enum
 * constant based on a given error code.
 * </p>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * AbisErrorCode errorCode = AbisErrorCode.INVALID_DECRYPTION_EXCEPTION;
 * String code = errorCode.getErrorCode(); // Retrieves "MOS-MABIS-003"
 * String message = errorCode.getErrorMessage(); // Retrieves "Data Decryption failure"
 * }</pre>
 * </p>
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum AbisErrorCode {
	INVALID_CONNECTION_EXCEPTION("MOS-MABIS-001", "Invalid connection"),
	INVALID_KEY_EXCEPTION("MOS-MABIS-002", "Value does not exists for key"),
	INVALID_DECRYPTION_EXCEPTION("MOS-MABIS-003", "Data Decryption failure"),
	INVALID_ID_EXCEPTION("MOS-MABIS-004", "Invalid Id value"),
	NO_VALUE_FOR_KEY_EXCEPTION("MOS-MABIS-005", "Value does not exists for key"),
	QUEUE_CONNECTION_NOT_FOUND_EXCEPTION("MOS-MABIS-006", "Queue Connection Not Found"),
	SET_EXPECTATION_EXCEPTION("MOS-MABIS-007", "Invalid set expectation"),
	GET_EXPECTATION_EXCEPTION("MOS-MABIS-008", "Invalid get expectation"),
	DELETE_EXPECTATION_EXCEPTION("MOS-MABIS-009", "Invalid delete expectation"),
	INVALID_CONFIGURATION_EXCEPTION("MOS-MABIS-010", "Invalid configure"),
	INVALID_CACHE_EXCEPTION("MOS-MABIS-010", "Invalid cache:"),
	DATA_NULL_OR_EMPTY_EXCEPTION("MOS-MABIS-011", "data is null or length is 0"),

	TECHNICAL_ERROR_EXCEPTION("MOS-MABIS-500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	/**
	 * Constructs an AbisErrorCode enum constant with the specified error code and
	 * error message.
	 *
	 * @param errorCode    The unique error code identifying the error
	 * @param errorMessage The descriptive error message explaining the error
	 */
	private AbisErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Retrieves the error code associated with this enum constant.
	 *
	 * @return The error code string
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Retrieves the error message associated with this enum constant.
	 *
	 * @return The error message string
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Retrieves the AbisErrorCode enum constant based on the given error code. If
	 * no matching enum constant is found, returns
	 * {@code TECHNICAL_ERROR_EXCEPTION}.
	 *
	 * @param errorCode The error code string to search for
	 * @return The matching AbisErrorCode enum constant, or
	 *         {@code TECHNICAL_ERROR_EXCEPTION} if not found
	 */
	public static AbisErrorCode fromErrorCode(String errorCode) {
		for (AbisErrorCode errorCodeEnum : AbisErrorCode.values()) {
			if (errorCodeEnum.getErrorCode().equalsIgnoreCase(errorCode)) {
				return errorCodeEnum;
			}
		}
		return TECHNICAL_ERROR_EXCEPTION;
	}
}