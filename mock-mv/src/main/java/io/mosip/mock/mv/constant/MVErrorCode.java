package io.mosip.mock.mv.constant;
/**
 * MVErrorCode Enum for the errors.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum MVErrorCode {
	CONFIGURE_EXCEPTION("MOS-MMV-001", "Exception while configuration"),
	GET_EXPECTATION_EXCEPTION("MOS-MMV-002", "Exception while getting expectation"),
	SET_EXPECTATION_EXCEPTION("MOS-MMV-003", "Exception while setting expectation"),
	DELETE_EXPECTATION_EXCEPTION("MOS-MMV-004", "Exception while deleting expectation"),
	INVALID_CONNECTION_EXCEPTION("MOS-MMV-005", "Invalid connection"),

	TECHNICAL_ERROR_EXCEPTION("MOS-MMV-500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	private MVErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static MVErrorCode fromErrorCode(String errorCode) {
		 for (MVErrorCode paramCode : MVErrorCode.values()) {
	     	if (paramCode.getErrorCode().equalsIgnoreCase(errorCode)) {
	        	return paramCode;
	    	}
	    }
		return TECHNICAL_ERROR_EXCEPTION;
	}
}