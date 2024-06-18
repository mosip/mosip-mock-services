package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for a biometric capture request.
 * 
 * This class encapsulates details about a request to capture biometric data
 * from a user.
 * 
 * @author [Your Name/Organization] (assuming you want to attribute authorship)
 * @since 1.0.0
 */
@Data
public class CaptureRequestDto {

	/**
	 * The environment in which the capture is taking place (e.g., "PRODUCTION",
	 * "TEST").
	 */
	private String env;

	/**
	 * The purpose for which the biometric data is being captured.
	 */
	private String purpose;

	/**
	 * The device specification version used by the capturing device (optional).
	 */
	private String specVersion;

	/**
	 * The maximum allowed time in milliseconds for the capture operation.
	 */
	private int timeout;

	/**
	 * The URI of the domain or application where the capture is taking place
	 * (private field).
	 */
	@JsonIgnore
	private String domainUri;

	/**
	 * The timestamp of the capture operation (private field).
	 */
	@JsonIgnore
	private String captureTime;

	/**
	 * A unique identifier for the capture transaction.
	 */
	private String transactionId;

	/**
	 * A list of capture details for each biometric modality to be captured
	 * (serialized as "bio").
	 */
	@JsonProperty("bio")
	private List<CaptureRequestDeviceDetailDto> bio;

	/**
	 * Custom options for the capture process (private field).
	 * 
	 * This field might be used to provide device-specific or application-specific
	 * configuration options for the capture operation. The format and meaning of
	 * these options depend on the implementation.
	 */
	@JsonIgnore
	private List<Map<String, String>> customOpts;

}