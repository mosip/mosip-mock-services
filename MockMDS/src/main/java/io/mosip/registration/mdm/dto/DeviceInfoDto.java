package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for response containing device information or
 * error details.
 * 
 * This class is used to represent the response from a request for device
 * information. The response can either contain the requested device information
 * or an error object if the request failed.
 * 
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties
public class DeviceInfoDto {
	/**
	 * The device information details as a JSON string (optional).
	 * 
	 * This field is populated if the request for device information was successful.
	 * It contains the details of the device in JSON format.
	 */
	private String deviceInfo;

	/**
	 * An error object containing details about any error encountered during
	 * processing (optional).
	 * 
	 * This field is populated if the request for device information failed. It
	 * contains an {@link ErrorInfo} object with the error code and message.
	 */
	private ErrorInfo error;
}