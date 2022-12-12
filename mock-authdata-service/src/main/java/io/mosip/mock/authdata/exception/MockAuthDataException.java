package io.mosip.mock.authdata.exception;

import io.mosip.mock.authdata.util.ErrorConstants;

public class MockAuthDataException extends RuntimeException {

	private String errorCode;

	public MockAuthDataException() {
        super(ErrorConstants.UNKNOWN_ERROR);
        this.errorCode = ErrorConstants.UNKNOWN_ERROR;
    }

	public MockAuthDataException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

	public String getErrorCode() {
		return errorCode;
	}
}
