package io.mosip.mock.authentication.exception;

import io.mosip.mock.authentication.util.ErrorConstants;

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
