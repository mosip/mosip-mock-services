package io.mosip.mock.authentication.exception;

import io.mosip.mock.authentication.util.ErrorConstants;

public class MockAuthenticationException extends RuntimeException {

	private String errorCode;

	public MockAuthenticationException() {
        super(ErrorConstants.UNKNOWN_ERROR);
        this.errorCode = ErrorConstants.UNKNOWN_ERROR;
    }

	public MockAuthenticationException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

	public String getErrorCode() {
		return errorCode;
	}
}
