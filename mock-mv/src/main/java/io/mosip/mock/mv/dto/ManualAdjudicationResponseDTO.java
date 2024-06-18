package io.mosip.mock.mv.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a manual adjudication response.
 * <p>
 * This class encapsulates various attributes related to a manual adjudication
 * response, including an identifier, request ID, response time, return value,
 * and a list of candidates.
 */
@Data
public class ManualAdjudicationResponseDTO {
	/**
	 * The unique identifier of the adjudication response.
	 */
	private String id;

	/**
	 * The identifier of the request associated with the adjudication response.
	 */
	private String requestId;

	/**
	 * The timestamp indicating when the response was generated.
	 */
	private String responsetime;

	/**
	 * The return value indicating the outcome of the adjudication process.
	 */
	private Integer returnValue;

	/**
	 * The list of candidates considered during the adjudication process.
	 */
	private CandidateList candidateList;

	/**
	 * Default constructor for ManualAdjudicationResponseDTO.
	 */
	public ManualAdjudicationResponseDTO() {
		super();
	}

	/**
	 * Constructs a ManualAdjudicationResponseDTO with specified parameters.
	 *
	 * @param id            The unique identifier of the adjudication response.
	 * @param requestId     The identifier of the request associated with the
	 *                      adjudication response.
	 * @param responsetime  The timestamp indicating when the response was
	 *                      generated.
	 * @param returnValue   The return value indicating the outcome of the
	 *                      adjudication process.
	 * @param candidateList The list of candidates considered during the
	 *                      adjudication process.
	 */
	@SuppressWarnings({ "1172" })
	public ManualAdjudicationResponseDTO(String id, String requestId, String responsetime, Integer returnValue,
			CandidateList candidateList) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.responsetime = responsetime;
		this.returnValue = returnValue;
		this.candidateList = candidateList;
	}
}