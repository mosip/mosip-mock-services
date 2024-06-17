package io.mosip.mock.mv.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a list of candidates for manual
 * adjudication.
 * <p>
 * This class encapsulates information about the candidates, including their
 * count, analytics related to the candidates, and a list of individual
 * candidate details.
 * <p>
 * It provides a structured format to manage and retrieve candidate information
 * for manual adjudication processes.
 * 
 */
@Data
public class CandidateList {
	/** The number of candidates in the list. */
	private Integer count;

	/** Analytics related to the candidates. */
	private Map<String, String> analytics;

	/** The list of individual candidates. */
	private List<Candidate> candidates;
}