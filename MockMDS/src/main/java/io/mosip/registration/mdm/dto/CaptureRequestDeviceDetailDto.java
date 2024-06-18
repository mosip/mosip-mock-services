package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for specifying capture details for a specific
 * biometric modality within a capture request.
 * 
 * This class is used to define the capture requirements for a particular
 * biometric modality (e.g., fingerprint, iris) within a
 * {@link CaptureRequestDto}.
 * 
 * @since 1.0.0
 */
@Data
public class CaptureRequestDeviceDetailDto {
	/**
	 * The type of biometric modality to be captured (e.g., "FINGERPRINT", "IRIS").
	 */
	private String type;

	/**
	 * The desired number of biometric samples to capture (e.g., "2").
	 */
	private String count; // Consider using int instead of String for count

	/**
	 * A list of exception codes that should not trigger capture failure (optional).
	 * 
	 * This field can be used to specify error codes that the device should tolerate
	 * during capture and allow the user to retry. The specific meaning of these
	 * codes depends on the device implementation.
	 */
	private String[] exception;

	/**
	 * A list of sub-types for the specified biometric modality (optional).
	 * 
	 * This field can be used to specify more specific requirements for the capture,
	 * such as capturing a specific finger or iris. The meaning of these sub-types
	 * depends on the device implementation and the supported biometric modalities.
	 */
	private String[] bioSubType;

	/**
	 * The minimum quality score required for a captured biometric sample.
	 */
	private int requestedScore;

	/**
	 * The identifier of the device to be used for capture (optional).
	 * 
	 * This field can be used to specify a particular device if multiple devices are
	 * available.
	 */
	private String deviceId;

	/**
	 * The sub-identifier of the device to be used for capture (optional).
	 * 
	 * This field might be used to specify a specific sensor on a multi-sensor
	 * device.
	 */
	private String deviceSubId;

	/**
	 * The hash value of a previously captured biometric sample (optional).
	 * 
	 * This field can be used for liveness detection or other comparison purposes.
	 * The format and interpretation of the hash value depend on the implementation.
	 */
	private String previousHash;
}