package io.mosip.mock.mv.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;
import io.mosip.mock.mv.service.MockMvDecisionService;

/**
 * Implementation of the {@link MockMvDecisionService} interface, providing
 * methods to manage mock decision values and expectations.
 * <p>
 * This class is annotated with {@link Service} to indicate that it is a
 * Spring-managed service bean.
 * </p>
 * 
 * @see MockMvDecisionService
 * @see ExpectationCache
 * @see Expectation
 */
@Service
public class MockMvDecisionServiceImpl implements MockMvDecisionService {
	/**
	 * The default mock decision value, injected from the application properties.
	 */
	@Value("${mock.mv.default.decision}")
	private String mockMvDecision;

	private ExpectationCache expectationCache;

	/**
	 * Constructs a new {@link MockMvDecisionServiceImpl} with the specified
	 * {@link ExpectationCache}.
	 * 
	 * @param expectationCache the expectation cache to use for storing and
	 *                         retrieving expectations.
	 */
	@Autowired
	public MockMvDecisionServiceImpl(ExpectationCache expectationCache) {
		this.expectationCache = expectationCache;
	}

	/**
	 * Returns the current mock decision value.
	 * 
	 * @return the current mock decision value.
	 */
	@Override
	public String getMockMvDecision() {
		return mockMvDecision;
	}

	/**
	 * Sets a new mock decision value.
	 * 
	 * @param mockDecision the new mock decision value to set.
	 */
	@Override
	public void setMockMvDecision(String mockDecision) {
		mockMvDecision = mockDecision;
	}

	/**
	 * Retrieves all expectations currently stored in the expectation cache.
	 * 
	 * @return a map of RId to {@link Expectation} objects.
	 */
	@Override
	public Map<String, Expectation> getExpectations() {
		return expectationCache.get();
	}

	/**
	 * Adds a new expectation to the expectation cache.
	 * 
	 * @param exp the {@link Expectation} object to add.
	 */
	@Override
	public void setExpectation(Expectation exp) {
		expectationCache.insert(exp);
	}

	/**
	 * Deletes the expectation associated with the specified RId from the cache.
	 * 
	 * @param id the RId of the expectation to delete.
	 */
	@Override
	public void deleteExpectation(String id) {
		expectationCache.delete(id);
	}

	/**
	 * Deletes all expectations from the expectation cache.
	 */
	@Override
	public void deleteExpectations() {
		expectationCache.deleteAll();
	}

	/**
	 * Retrieves the expectation associated with the specified RId from the cache.
	 * If the RId is not found, returns a new {@link Expectation} object.
	 * 
	 * @param rid the RId of the expectation to retrieve.
	 * @return the {@link Expectation} object associated with the provided RId, or a
	 *         new {@link Expectation} if not found.
	 */
	@Override
	public Expectation getExpectation(String rid) {
		return expectationCache.get(rid);
	}
}