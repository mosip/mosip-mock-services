package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for response containing discovered device details
 * or error information.
 * 
 * This class is used to represent the response from a device discovery request.
 * The response can either contain details about a discovered device or an error
 * object if no device was found or an error occurred during discovery.
 * 
 * @since 1.0.0
 */
@Data
public class DiscoverDto {
	/**
	 * The unique identifier of the discovered device.
	 * 
	 * This field is populated if a device was successfully discovered.
	 */
	private String deviceId;

	/**
	 * The current status of the discovered device (e.g., "READY", "BUSY").
	 */
	private String deviceStatus;

	/**
	 * The certification information of the discovered device.
	 */
	private String certification;

	/**
	 * The service version of the discovered device software.
	 */
	private String serviceVersion;

	/**
	 * The callback identifier associated with the discovered device.
	 */
	private String callbackId;

	/**
	 * The Digital ID associated with the discovered device.
	 */
	private String digitalId;

	/**
	 * The code identifying the model of the discovered device.
	 */
	private String deviceCode;

	/**
	 * The purpose for which the discovered device is currently being used.
	 */
	private String purpose;

	/**
	 * An error object containing details about any error encountered during
	 * discovery .
	 * 
	 * This field is populated if the device discovery failed. It contains an
	 * {@link ErrorInfo} object with the error code and message.
	 */
	private ErrorInfo error;

	/**
	 * The device specification version(s) as an array of strings .
	 * 
	 * The specific format and meaning of the version strings depend on the device
	 * implementation.
	 */
	private String[] specVersion;

	/**
	 * The device sub-identifier(s) as an array of strings.
	 * 
	 * This could provide more specific information about the modalities supported
	 * by the device (e.g., for multiple sensors).
	 */
	private String[] deviceSubId;
}