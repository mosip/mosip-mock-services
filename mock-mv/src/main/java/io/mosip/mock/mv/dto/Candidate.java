package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing an individual candidate for manual
 * adjudication.
 * <p>
 * This class encapsulates information about a candidate, including their unique
 * reference ID and analytics related to the candidate's verification process.
 * <p>
 * It provides a structured format to manage and retrieve candidate details
 * within the context of manual adjudication workflows.
 * 
 */
@Data
public class Candidate {
	/** The unique reference ID of the candidate. */
	private String referenceId;

	/** Analytics related to the candidate's verification process. */
	private AnalyticsDTO analytics;
}