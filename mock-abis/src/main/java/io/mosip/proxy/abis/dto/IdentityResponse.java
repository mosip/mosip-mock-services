package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.proxy.abis.dto.IdentityRequest.Flags;
import io.mosip.proxy.abis.dto.IdentityRequest.Gallery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IdentityResponse represents the response structure for an identity search
 * operation in the ABIS (Automated Biometric Identification System).
 * <p>
 * This class encapsulates fields such as ID, request ID, response time, return
 * value, candidate list, and analytics information. It provides a structured
 * format for conveying identity search results including candidate details and
 * analytical data.
 * </p>
 * <p>
 * Nested classes within IdentityResponse include CandidateList, Modalities,
 * Analytics, and Candidates, each serving specific roles in organizing and
 * detailing the response data.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityResponse {
	private String id;
	private String requestId;
	private LocalDateTime responsetime;
	private String returnValue;
	private CandidateList candidateList;
	private Analytics analytics = new Analytics();

	/**
	 * CandidateList encapsulates the list of candidates returned in the identity
	 * response.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CandidateList {
		private String count;
		private List<Candidates> candidates;
	}

	/**
	 * Modalities represents different biometric modalities associated with a
	 * candidate.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Modalities {
		private String biometricType;
		private Analytics analytics;
	}

	/**
	 * Analytics provides analytical data associated with a specific aspect of the
	 * identity response.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Analytics {
		private String confidence;
		private String internalScore;
		private String key1;
		private String key2;
	}

	/**
	 * Candidates represent individuals matching the identity search criteria.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Candidates {
		private String referenceId;
		private Analytics analytics;
		private List<Modalities> modalities;
	}
}