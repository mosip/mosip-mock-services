package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for requesting a delay in a process.
 * 
 * This class is used to encapsulate details about a request to delay a specific
 * process.
 * 
 * @since 1.0.0
 */
@Data
public class DelayRequest {
	/**
	 * The type of process for which a delay is being requested. (The specific
	 * meaning of "type" depends on the implementation, but it could indicate
	 * different processes that can be delayed, e.g., "Face", "Iris").
	 */
	private String type;

	/**
	 * The desired duration of the delay in milliseconds.
	 */
	private String delay; // Consider using a long instead of String for delay duration

	/**
	 * The method(s) to be used for notifying the user about the delay (optional).
	 * 
	 * This field can be an array of strings representing different notification
	 * methods available (e.g., "RCAPTURE", "CAPTURE").
	 */
	private String[] method;
}