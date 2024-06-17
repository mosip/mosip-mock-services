package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for Digital ID information.
 * 
 * This class encapsulates details extracted from a Digital ID source (which
 * might be a physical device or a logical representation).
 * 
 * @since 1.0.0
 */
@Data
public class DigitalId {
	/**
	 * The serial number associated with the Digital ID .
	 */
	private String serialNo;

	/**
	 * The manufacturer of the device or source associated with the Digital ID.
	 */
	private String make;

	/**
	 * The model of the device or source associated with the Digital ID.
	 */
	private String model;

	/**
	 * The type of Digital ID (e.g., "FINGERPRINT", "IRIS", "FACE").
	 */
	private String type;

	/**
	 * The sub-type of the Digital ID device or source (optional).
	 * 
	 * This could provide more specific information about the modality used for
	 * capturing the biometric data (e.g., "Left Iris").
	 */
	private String deviceSubType;

	/**
	 * The identifier of the provider associated with the Digital ID\.
	 */
	private String deviceProviderId;

	/**
	 * The name of the provider associated with the Digital ID.
	 */
	private String deviceProvider;

	/**
	 * The date and time when the Digital ID information was captured or retrieved.
	 */
	private String dateTime;
}