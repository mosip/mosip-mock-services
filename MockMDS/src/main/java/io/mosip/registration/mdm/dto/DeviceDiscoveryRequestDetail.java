package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for device discovery request details.
 * 
 * This class is used to encapsulate information related to a request for
 * discovering nearby devices.
 * 
 * @since 1.0.0
 */
@Data
public class DeviceDiscoveryRequestDetail {
	/**
	 * The type of device discovery requested. (The specific meaning of "type"
	 * depends on the implementation, but it could indicate different discovery
	 * methods available, e.g., "Face", "Iris").
	 */
	private String type;
}