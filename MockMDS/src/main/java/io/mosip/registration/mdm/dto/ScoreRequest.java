package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for score request details.
 * 
 * This class is used to encapsulate information related to requesting a score
 * from a biometric device.
 * 
 * @since 1.0.0
 */
@Data
public class ScoreRequest {
	/**
	 * The type of score requested from the device. (The specific meaning of "type"
	 * depends on the implementation, but it could indicate different types of
	 * function.
	 */
	private String type;

	/**
	 * The quality score reported by the device for the captured biometric data
	 * (optional).
	 * 
	 * This field can be populated by the device itself if the request includes
	 * retrieving the quality score along with the requested score type.
	 */
	private String qualityScore;

	/**
	 * A flag indicating whether the score should be retrieved from the ISO image
	 * (if available) instead of the captured data.
	 */
	private boolean fromIso;
}