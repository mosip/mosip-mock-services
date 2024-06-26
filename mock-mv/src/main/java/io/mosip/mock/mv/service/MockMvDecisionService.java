package io.mosip.mock.mv.service;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;

/**
 * Interface for managing mock MV (Matching Verification) decisions and
 * expectations.
 * <p>
 * This interface provides methods to set and retrieve the mock MV decision as
 * well as to manage expectations related to mock MV.
 * </p>
 * 
 * @see Expectation
 */
public interface MockMvDecisionService {

	/**
	 * Retrieves the current mock MV decision.
	 * 
	 * @return the current mock MV decision as a {@code String}.
	 */
	public String getMockMvDecision();

	/**
	 * Sets a new mock MV decision.
	 * 
	 * @param mockMvDecision the new mock MV decision to set.
	 */
	public void setMockMvDecision(String mockMvDecision);

	/**
	 * Retrieves all expectations currently stored.
	 * 
	 * @return a map of RId to {@link Expectation} objects.
	 */
	public Map<String, Expectation> getExpectations();

	/**
	 * Retrieves the expectation associated with the specified RId.
	 * 
	 * @param rid the unique request identifier of the expectation to retrieve.
	 * @return the {@link Expectation} object associated with the provided RId, or
	 *         {@code null} if no such expectation exists.
	 */
	public Expectation getExpectation(String rid);

	/**
	 * Sets a new expectation.
	 * 
	 * @param exp the {@link Expectation} object to set.
	 */
	public void setExpectation(Expectation exp);

	/**
	 * Deletes the expectation associated with the specified RId.
	 * 
	 * @param rid the unique request identifier of the expectation to delete.
	 */
	public void deleteExpectation(String rid);

	/**
	 * Deletes all expectations.
	 */
	public void deleteExpectations();
}