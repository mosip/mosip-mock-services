package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for device status request details.
 * 
 * This class is used to encapsulate information related to a request for a
 * device's status.
 * 
 * @since 1.0.0
 */
@Data
public class StatusRequest {
	/**
	 * The type of status information requested. (The specific meaning of "type"
	 * depends on the implementation, but it could indicate different categories of
	 * status information available from the device).
	 */
	private String type;

	/**
	 * The current device status reported by the device itself (e.g., "READY",
	 * "BUSY" . etc).
	 */
	private String deviceStatus;
}