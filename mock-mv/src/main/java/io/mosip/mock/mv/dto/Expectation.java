package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing an expectation for mock MV decision.
 * <p>
 * This class encapsulates the details of an expectation, including the request
 * ID (RID), mock MV decision, and optional delay in response.
 */
@Data
public class Expectation {

	/**
	 * The request ID (RID) associated with the expectation.
	 */
	private String rId;

	/**
	 * The mock MV decision for the expectation.
	 */
	private String mockMvDecision;

	/**
	 * Optional delay in response, in milliseconds.
	 */
	private int delayResponse;

	/**
	 * Default constructor.
	 */
	public Expectation() {
		super();
	}

	/**
	 * Constructs an Expectation object with specified attributes.
	 *
	 * @param rId            The request ID (RID) associated with the expectation.
	 * @param mockMvDecision The mock MV decision for the expectation.
	 * @param delayResponse  Optional delay in response, in milliseconds.
	 */
	public Expectation(String rId, String mockMvDecision, int delayResponse) {
		super();
		this.rId = rId;
		this.mockMvDecision = mockMvDecision;
		this.delayResponse = delayResponse;
	}

	/**
	 * Retrieves the mock MV decision associated with the expectation.
	 *
	 * @return The mock MV decision.
	 */
	public String getMockMvDecision() {
		return mockMvDecision;
	}

	/**
	 * Generates a string representation of the Expectation object.
	 *
	 * @return A string representation containing the attributes of the Expectation.
	 */
	@Override
	public String toString() {
		return "Expectation{" + "rId='" + rId + '\'' + ", mockMvDecision='" + mockMvDecision + '\'' + ", delayResponse="
				+ delayResponse + '}';
	}
}