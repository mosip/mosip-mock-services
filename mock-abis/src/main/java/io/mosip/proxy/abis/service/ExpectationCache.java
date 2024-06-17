package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.dto.Expectation;

import java.util.Map;

/**
 * Interface defining operations for managing a cache of {@link Expectation}
 * objects. This includes functionalities for inserting, retrieving, and
 * deleting expectations from the cache.
 */
public interface ExpectationCache {
	/**
	 * Deletes a specific expectation from the cache.
	 *
	 * @param id A {@link String} representing the identifier of the expectation to
	 *           be deleted.
	 * @return A {@link boolean} indicating whether the deletion was successful.
	 */
	public boolean delete(String id);

	/**
	 * Deletes all expectations from the cache.
	 */
	public void deleteAll();

	/**
	 * Inserts a new expectation into the cache.
	 *
	 * @param ie An {@link Expectation} object representing the expectation to be
	 *           inserted.
	 */
	public void insert(Expectation ie);

	/**
	 * Retrieves an expectation from the cache by its unique identifier.
	 *
	 * @param id The unique identifier of the expectation to be retrieved.
	 * @return The {@link Expectation} object corresponding to the given identifier,
	 *         or {@code null} if no such expectation exists.
	 */
	public Expectation get(String id);

	/**
	 * Retrieves all expectations from the cache.
	 *
	 * @return A {@link Map} where the keys are expectation identifiers and the
	 *         values are {@link Expectation} objects.
	 */
	public Map<String, Expectation> get();
}