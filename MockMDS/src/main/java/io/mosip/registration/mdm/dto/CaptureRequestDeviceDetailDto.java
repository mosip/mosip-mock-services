package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
	@JsonProperty(value = "type", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String type;

	/**
	 * The desired number of biometric samples to capture (e.g., "2").
	 */
	@JsonProperty(value = "count", required = true)
	@JsonDeserialize(using = IntegerDeserializer.class)
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
	@JsonProperty(value = "requestedScore", required = true)
	@JsonDeserialize(using = IntegerDeserializer.class)
	private String requestedScore;

	/**
	 * The identifier of the device to be used for capture (optional).
	 * 
	 * This field can be used to specify a particular device if multiple devices are
	 * available.
	 */
	@JsonProperty(value = "deviceId", required = true)
	@JsonDeserialize(using = StringDeserializer.class)
	private String deviceId;

	/**
	 * The sub-identifier of the device to be used for capture (optional).
	 * 
	 * This field might be used to specify a specific sensor on a multi-sensor
	 * device.
	 */
	@JsonProperty(value = "deviceSubId", required = true)
	@JsonDeserialize(using = IntegerDeserializer.class)
	private String deviceSubId;

	/**
	 * The hash value of a previously captured biometric sample (optional).
	 * 
	 * This field can be used for liveness detection or other comparison purposes.
	 * The format and interpretation of the hash value depend on the implementation.
	 */
	@JsonProperty(value = "previousHash", required = true)
	private String previousHash;

	// Define the validation method
	public boolean validateCaptureRequestDeviceDetail() {
		if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("type is required and must not be null or empty.");
        }
        if (count == null || count.trim().isEmpty()) {
            throw new IllegalArgumentException("count is required and must not be null or empty.");
        }
        if (requestedScore == null || requestedScore.trim().isEmpty()) {
            throw new IllegalArgumentException("requestedScore is required and must not be null or empty.");
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("deviceId is required and must not be null or empty.");
        }
        if (deviceSubId == null || deviceSubId.trim().isEmpty()) {
            throw new IllegalArgumentException("deviceSubId is required and must not be null or empty.");
        }
        if (previousHash == null) {
            throw new IllegalArgumentException("previousHash is required and must not be null.");
        }
        
		return validateStringArray(exception, "exception") || validateStringArray(bioSubType, "bioSubType") || 
				validateStringCanBeEmpty(previousHash, "previousHash");
	}
	
	private boolean validateStringCanBeEmpty(String value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " must not be null.");
		}
		return true;
	}

	private boolean validateStringArray(String[] values, String fieldName) {
		if (values == null)
			return true;
		for (Object val : values) {
			if (!(val instanceof String)) {
				throw new IllegalArgumentException(fieldName + " all elements in the array must be Strings.");
			}
		}
		return true;
	}
}