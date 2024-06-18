package io.mosip.mock.sdk.constant;

/**
 * Enumeration of response status codes used in the SDK.
 * 
 * <p>
 * This enum defines standard HTTP-like status codes and their associated
 * messages used for indicating the result of SDK operations.
 * </p>
 * 
 */
public enum ResponseStatus {
	/**
	 * Success status.
	 */
	SUCCESS(200, "OK"),

	/**
	 * Invalid input parameter status.
	 */
	INVALID_INPUT(401, "Invalid Input Parameter"),

	/**
	 * Missing input parameter status.
	 */
	MISSING_INPUT(402, "Missing Input Parameter"),

	/**
	 * Biometric data quality check failed status.
	 */
	QUALITY_CHECK_FAILED(403, "Quality check of Biometric data failed"),

	/**
	 * Biometrics not found in CBEFF status.
	 */
	BIOMETRIC_NOT_FOUND_IN_CBEFF(404, "Biometrics not found in CBEFF"),

	/**
	 * Matching of biometric data failed status.
	 */
	MATCHING_OF_BIOMETRIC_DATA_FAILED(405, "Matching of Biometric data failed"),

	/**
	 * Data provided is of poor quality status.
	 */
	POOR_DATA_QUALITY(406, "Data provided is of poor quality"),

	/**
	 * Unknown error status.
	 */
	UNKNOWN_ERROR(500, "UNKNOWN_ERROR");

	private int statusCode;
	private String statusMessage;

	/**
	 * Constructor for ResponseStatus enum.
	 * 
	 * @param statusCode    The HTTP-like status code.
	 * @param statusMessage The message associated with the status code.
	 */
	ResponseStatus(int statusCode, String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	/**
	 * Retrieves the status code.
	 * 
	 * @return The integer status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Retrieves the status message.
	 * 
	 * @return The string status message.
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Retrieves the ResponseStatus enum based on the provided status code.
	 * 
	 * @param code The status code to match against.
	 * @return The corresponding ResponseStatus enum, or UNKNOWN_ERROR if no match
	 *         is found.
	 */
	public static ResponseStatus fromStatusCode(int code) {
		for (ResponseStatus paramCode : ResponseStatus.values()) {
			if (paramCode.getStatusCode() == code) {
				return paramCode;
			}
		}
		return UNKNOWN_ERROR;
	}
}