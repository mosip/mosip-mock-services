package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for device information.
 * 
 * This class encapsulates details about a biometric device.
 * 
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties
public class DeviceInfo {
	/**
	 * The device specification version(s) as an array of strings.
	 * 
	 * The specific format and meaning of the version strings depend on the device
	 * implementation.
	 */
	private String[] specVersion;

	/**
	 * The environment in which the device is operating (e.g., "PRODUCTION",
	 * "TEST").
	 */
	private String env;

	/**
	 * The Digital ID associated with the device (optional).
	 */
	private String digitalId;

	/**
	 * The unique identifier of the device.
	 */
	private String deviceId;

	/**
	 * The code identifying the device model.
	 */
	private String deviceCode;

	/**
	 * The purpose for which the device is currently being used.
	 */
	private String purpose;

	/**
	 * The service version of the device software.
	 */
	private String serviceVersion;

	/**
	 * The current status of the device (e.g., "READY", "BUSY").
	 */
	private String deviceStatus;

	/**
	 * The firmware version of the device.
	 */
	private String firmware;

	/**
	 * The device certification information (optional).
	 */
	private String certification;

	/**
	 * The device sub-identifier(s) as an array of strings (e.g., for multiple
	 * sensors).
	 */
	private String[] deviceSubId;

	/**
	 * The callback identifier associated with the device (optional).
	 */
	private String callbackId;
}