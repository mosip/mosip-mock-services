package io.mosip.mock.mv.constant;

/**
 * Enumeration representing error codes and messages for MV (Mock Verification)
 * module.
 * <p>
 * Each enum constant provides a unique error code and corresponding error
 * message. The {@code fromErrorCode} method allows retrieving enum constants
 * based on error code.
 * 
 * @since 1.0.0
 * @author Janardhan B S
 */
public enum MVErrorCode {

	/**
	 * Exception while configuration.
	 */
	CONFIGURE_EXCEPTION("MOS-MMV-001", "Exception while configuration"),

	/**
	 * Exception while getting expectation.
	 */
	GET_EXPECTATION_EXCEPTION("MOS-MMV-002", "Exception while getting expectation"),

	/**
	 * Exception while setting expectation.
	 */
	SET_EXPECTATION_EXCEPTION("MOS-MMV-003", "Exception while setting expectation"),

	/**
	 * Exception while deleting expectation.
	 */
	DELETE_EXPECTATION_EXCEPTION("MOS-MMV-004", "Exception while deleting expectation"),

	/**
	 * Invalid connection error.
	 */
	INVALID_CONNECTION_EXCEPTION("MOS-MMV-005", "Invalid connection"),

	/**
	 * General technical error.
	 */
	TECHNICAL_ERROR_EXCEPTION("MOS-MMV-500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	/**
	 * Constructs an MVErrorCode enum constant with the specified error code and
	 * message.
	 * 
	 * @param errorCode    the error code associated with the error
	 * @param errorMessage the error message describing the error
	 */
	private MVErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the error code.
	 * 
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the error message.
	 * 
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Retrieves the MVErrorCode enum constant based on the specified error code.
	 * 
	 * @param errorCode the error code to look up
	 * @return the MVErrorCode enum constant corresponding to the error code, or
	 *         {@link #TECHNICAL_ERROR_EXCEPTION} if no match is found
	 */
	public static MVErrorCode fromErrorCode(String errorCode) {
		for (MVErrorCode errorCodeEnum : MVErrorCode.values()) {
			if (errorCodeEnum.getErrorCode().equalsIgnoreCase(errorCode)) {
				return errorCodeEnum;
			}
		}
		return TECHNICAL_ERROR_EXCEPTION;
	}
}