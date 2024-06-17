package io.mosip.registration.mdm.dto;

import java.util.List;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for response containing captured biometric data.
 * 
 * This class is used to represent the response from a biometric capture
 * operation.
 * 
 * @since 1.0.0
 */
@Data
public class RCaptureResponse {
	/**
	 * A List containing BioMetricsDto objects representing the captured biometric
	 * data.
	 * 
	 * Each BioMetricsDto object encapsulates details about a specific captured
	 * biometric modality (e.g., fingerprint, iris, face) and the associated data.
	 */
	private List<BioMetricsDto> biometrics;
}