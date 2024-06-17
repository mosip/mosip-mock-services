package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for profile request details.
 * 
 * This class is used to encapsulate information related to a request for a
 * biometric profile.
 * 
 * @since 1.0.0
 */
@Data
public class ProfileRequest {
	/**
	 * The type of profile information requested. (The specific meaning of "type"
	 * depends on the implementation, but it could indicate different types of
	 * profiles available, e.g., "Face", "Iris").
	 */
	private String type;

	/**
	 * The identifier of the specific profile being requested.
	 * 
	 * The meaning and format of the profile ID depend on the type of profile
	 * requested and the specific implementation.
	 */
	private String profileId;
}