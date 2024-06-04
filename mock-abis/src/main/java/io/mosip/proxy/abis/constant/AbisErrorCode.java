package io.mosip.proxy.abis.constant;
/**
 * AbisErrorCode Enum for the errors.
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

	private AbisErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static AbisErrorCode fromErrorCode(String errorCode) {
		 for (AbisErrorCode paramCode : AbisErrorCode.values()) {
	     	if (paramCode.getErrorCode().equalsIgnoreCase(errorCode)) {
	        	return paramCode;
	    	}
	    }
		return TECHNICAL_ERROR_EXCEPTION;
	}
}