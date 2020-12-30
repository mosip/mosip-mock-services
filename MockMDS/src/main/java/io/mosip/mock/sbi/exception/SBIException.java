package io.mosip.mock.sbi.exception;

public class SBIException extends Exception {
	/** Serializable version Id. */
	private static final long serialVersionUID = 2289885935924708933L;

	/**
	 * @param errorCode Corresponds to Particular Exception
	 * @param errorMessage Message providing the specific context of the error.
	 * @param rootCause Cause of exception
	 */
	public SBIException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorMessage, rootCause);
	}
}
