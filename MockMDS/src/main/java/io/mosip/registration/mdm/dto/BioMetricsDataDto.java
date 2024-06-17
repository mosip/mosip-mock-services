package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for a specific captured biometric sample.
 * 
 * This class represents detailed information about a single captured biometric
 * sample.
 * 
 * @since 1.0.0
 */
@Data
public class BioMetricsDataDto {
	/**
	 * The Digital ID associated with the captured biometric sample.
	 */
	private String digitalId;

	/**
	 * The code identifying the model of the device used for capture.
	 */
	private String deviceCode;

	/**
	 * The service version of the device software used for capture.
	 */
	private String deviceServiceVersion;

	/**
	 * The type of biometric modality captured (e.g., "FINGERPRINT", "IRIS").
	 */
	private String bioType;

	/**
	 * The sub-type of the captured biometric modality.
	 * 
	 * This field can provide more specific information about the captured sample,
	 * such as the specific finger or iris captured.
	 */
	private String bioSubType;

	/**
	 * The purpose for which the biometric sample was captured.
	 */
	private String purpose;

	/**
	 * The environment in which the capture took place (e.g., "PRODUCTION", "TEST").
	 */
	private String env;

	/**
	 * The URI of the domain or application where the capture took place.
	 */
	private String domainUri;

	/**
	 * The captured biometric data in a format specific to the modality (e.g.,
	 * base64 encoded).
	 */
	private String bioValue;

	/**
	 * The extracted biometric data from the captured sample.
	 * 
	 * This field might contain data in a format suitable for matching or
	 * verification, depending on the implementation. The specific format and
	 * meaning of this data depend on the biometric modality.
	 */
	private String bioExtract;

	/**
	 * The identifier of the registration process for which the biometric sample was
	 * captured.
	 */
	private String registrationId;

	/**
	 * The unique identifier of the capture transaction.
	 */
	private String transactionId;

	/**
	 * The timestamp of the capture operation.
	 */
	private String timestamp;

	/**
	 * The minimum quality score required for the captured sample.
	 * 
	 * This field might be included for informational purposes, indicating the
	 * pre-defined quality threshold for the capture.
	 */
	private String requestedScore;

	/**
	 * The actual quality score of the captured biometric sample.
	 */
	private String qualityScore;
}