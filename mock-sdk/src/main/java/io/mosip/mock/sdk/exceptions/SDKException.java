package io.mosip.mock.sdk.exceptions;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom Exception Class in case of error occurred in services for Test Case.
 * 
 * @see io.mosip.kernel.core.exception.BaseUncheckedException
 * @author Janardhan B S
 * @since 1.0.0
 */
public class SDKException extends BaseUncheckedException {
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 687991492884005033L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public SDKException(String errorCode, String errorMessage) {
		super(errorMessage);
		addInfo(errorCode, errorMessage);
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public SDKException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}