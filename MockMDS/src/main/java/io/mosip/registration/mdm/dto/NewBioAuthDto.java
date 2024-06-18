package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for newly captured biometric data for
 * authentication purposes.
 * 
 * This class is a specialization of {@link NewBioDto} used specifically for
 * biometric data captured during an authentication process. It includes an
 * additional field for the domain URI.
 * 
 * @since 1.0.0
 */
@Data
public class NewBioAuthDto {
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

	/**
	 * The Domain URI associated with the authentication process.
	 * 
	 * This field can be used to identify the specific domain or application where
	 * the biometric authentication is taking place.
	 */
	private String domainUri;
}