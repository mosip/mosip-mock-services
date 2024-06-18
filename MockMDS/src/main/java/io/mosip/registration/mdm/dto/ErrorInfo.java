package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for error information.
 * 
 * This class is used to encapsulate details about an error encountered during
 * processing.
 * 
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties
@SuppressWarnings({ "java:S1700" })
public class ErrorInfo {
	/**
	 * The error code identifying the specific type of error encountered.
	 */
	private String errorCode;
	/**
	 * A human-readable description of the error that occurred.
	 */
	private String errorInfo;

	/**
	 * Constructor to create an ErrorInfo object.
	 * 
	 * @param errorCode The error code for the encountered error.
	 * @param errorInfo A description of the error that occurred.
	 */
	public ErrorInfo(String errorCode, String errorInfo) {
		super();
		this.errorCode = errorCode;
		this.errorInfo = errorInfo;
	}
}