package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for newly captured biometric data.
 * 
 * This class encapsulates details about a newly captured biometric sample along
 * with relevant metadata for processing.
 * 
 * @since 1.0.0
 */
@Data
public class NewBioDto {
	/**
	 * The Digital ID associated with the captured biometric data (optional).
	 */
	private String digitalId;

	/**
	 * The code identifying the device that captured the biometric data.
	 */
	private String deviceCode;

	/**
	 * The service version of the device that captured the biometric data.
	 */
	private String deviceServiceVersion;

	/**
	 * The specific biometric sub-type captured (e.g., "Left Iris").
	 */
	private String bioSubType;

	/**
	 * The purpose for which the biometric data was captured (e.g., "Registration",
	 * "Auth").
	 */
	private String purpose;

	/**
	 * The environment in which the capture occurred (e.g., "PRODUCTION", "TEST").
	 */
	private String env;

	/**
	 * The main biometric type captured (e.g., "FINGER", "IRIS", "FACE").
	 */
	private String bioType;

	/**
	 * The actual biometric data captured (format depends on the specific biometric
	 * type).
	 */
	private String bioValue;

	/**
	 * The unique identifier for the capture transaction.
	 */
	private String transactionId;

	/**
	 * The timestamp of the biometric capture.
	 */
	private String timestamp;

	/**
	 * The score requested from the device during capture (optional).
	 */
	private String requestedScore;

	/**
	 * The quality score reported by the device for the captured data (optional).
	 */
	private String qualityScore;
}