package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for captured biometric data.
 * 
 * This class encapsulates details about captured biometric data from a user.
 * 
 * @author [Your Name/Organization] (assuming you want to attribute authorship)
 * @since [version] (assuming you want to specify the version where this class
 *        was introduced)
 */
@Data
@JsonIgnoreProperties
public class BioMetricsDto {
	/**
	 * The device specification version used during capture (optional).
	 */
	private String specVersion;

	/**
	 * The captured biometric data in a format specific to the modality (e.g.,
	 * base64 encoded).
	 */
	private String data;

	/**
	 * The hash of the captured biometric data (optional).
	 * 
	 * This field might be used for security or verification purposes. The specific
	 * hashing algorithm used depends on the implementation.
	 */
	private String hash;

	/**
	 * The session key used to encrypt the captured biometric data (optional).
	 * 
	 * This field can be used to ensure the confidentiality of the biometric data.
	 */
	private String sessionKey;

	/**
	 * A fingerprint (likely a hash) representing a summary of the captured
	 * biometric data (optional).
	 * 
	 * This field can be used for deduplication or other comparison purposes. The
	 * specific format and meaning of the fingerprint depend on the implementation.
	 */
	private String thumbprint;

	/**
	 * An error object containing details about any error encountered during capture
	 * (optional).
	 * 
	 * This field is populated if the biometric capture failed. It contains an
	 * {@link ErrorInfo} object with the error code and message.
	 */
	private ErrorInfo error;

	/**
	 * Additional capture-related information (private field).
	 * 
	 * This field can be used to store device-specific or modality-specific details
	 * about the capture process. The format and meaning of this information depend
	 * on the implementation.
	 */
	@JsonIgnore
	private List<Map<String, String>> additionalInfo;
}