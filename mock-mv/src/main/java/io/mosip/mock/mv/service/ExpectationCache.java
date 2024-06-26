package io.mosip.mock.mv.service;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;

/**
 * Interface for caching and managing {@link Expectation} objects.
 * <p>
 * This interface provides methods for inserting, retrieving, and deleting
 * expectations, which are mapped by their unique request identifier (RId).
 * </p>
 * 
 * @see Expectation
 */
public interface ExpectationCache {

	/**
	 * Deletes the expectation associated with the specified RId from the cache.
	 * 
	 * @param rid the unique request identifier of the expectation to delete.
	 * @return {@code true} if the expectation was successfully deleted,
	 *         {@code false} otherwise.
	 */
	public boolean delete(String rid);

	/**
	 * Deletes all expectations from the cache.
	 */
	public void deleteAll();

	/**
	 * Inserts a new expectation into the cache.
	 * 
	 * @param expectation the {@link Expectation} object to insert.
	 */
	public void insert(Expectation expectation);

	/**
	 * Retrieves all expectations currently stored in the cache.
	 * 
	 * @return a map of RId to {@link Expectation} objects.
	 */
	public Map<String, Expectation> get();

	/**
	 * Retrieves the expectation associated with the specified RId from the cache.
	 * 
	 * @param rid the unique request identifier of the expectation to retrieve.
	 * @return the {@link Expectation} object associated with the provided RId, or
	 *         {@code null} if no such expectation exists.
	 */
	public Expectation get(String rid);
}