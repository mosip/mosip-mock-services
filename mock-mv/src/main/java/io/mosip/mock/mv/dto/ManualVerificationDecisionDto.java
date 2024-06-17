package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a manual verification decision.
 * <p>
 * This class encapsulates various attributes related to a manual verification
 * decision, including the matched reference type, user ID performing the
 * verification, reason code for the decision, registration ID, and status code.
 */
@Data
public class ManualVerificationDecisionDto {
	/**
	 * The type of reference that was matched during manual verification.
	 */
	private String matchedRefType;

	/**
	 * The user ID of the person who performed the manual verification.
	 */
	private String mvUsrId;

	/**
	 * The reason code associated with the manual verification decision.
	 */
	private String reasonCode;

	/**
	 * The registration ID associated with the entity being verified manually.
	 */
	private String regId;

	/**
	 * The status code indicating the outcome of the manual verification decision.
	 */
	private String statusCode;
}