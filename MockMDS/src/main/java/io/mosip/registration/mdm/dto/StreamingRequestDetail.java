package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for streaming request details.
 * 
 * This class is used to encapsulate information related to a streaming request
 * sent to a mobile device.
 * 
 * @since 1.0.0
 */

@Data
public class StreamingRequestDetail {
	/**
	 * The unique identifier of the device initiating the streaming request.
	 */
	private String deviceId;

	/**
	 * The sub-identifier of the device, specific to the modality or sensor used for
	 * streaming (e.g., fingerprint sensor number).
	 */
	private String deviceSubId;

	/**
	 * The timeout value for the streaming request, specified in milliseconds.
	 */
	private String timeout;
}