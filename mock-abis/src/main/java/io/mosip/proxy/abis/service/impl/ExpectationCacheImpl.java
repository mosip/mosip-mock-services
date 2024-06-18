package io.mosip.proxy.abis.service.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ExpectationCache;

/**
 * Implementation of the {@link ExpectationCache} interface using
 * {@link ConcurrentHashMap}.
 * <p>
 * This class manages expectations stored in memory using a thread-safe
 * {@link ConcurrentHashMap}. Expectations are stored and retrieved based on
 * unique identifiers.
 * </p>
 * <p>
 * Use this implementation to handle caching and retrieval of
 * {@link Expectation} objects, facilitating efficient management of
 * expectations in ABIS (Automated Biometric Identification System) or similar
 * systems.
 * </p>
 * 
 * @since 1.0.0
 */
@Component
public class ExpectationCacheImpl implements ExpectationCache {
	/** Map to store expectations, keyed by their unique identifier. */
	private Map<String, Expectation> expectationMap = new ConcurrentHashMap<>();

	/**
	 * Deletes the expectation associated with the given identifier.
	 * 
	 * @param id The unique identifier of the expectation to delete.
	 * @return {@code true} if an expectation was removed, {@code false} otherwise.
	 */
	public boolean delete(String id) {
		return expectationMap.remove(id) != null;
	}

	/**
	 * Clears all expectations from the cache.
	 */
	public void deleteAll() {
		if (!Objects.isNull(expectationMap))
			expectationMap.clear();
		else
			expectationMap = new ConcurrentHashMap<>();
	}

	/**
	 * Inserts a new expectation into the cache or updates an existing one.
	 * 
	 * @param expectation The expectation to insert or update.
	 */
	public void insert(Expectation expectation) {
		expectationMap.put(expectation.getId(), expectation);
	}

	/**
	 * Retrieves the expectation associated with the given identifier. If no
	 * matching expectation is found, returns a default {@link Expectation} object.
	 * 
	 * @param id The unique identifier of the expectation to retrieve.
	 * @return The {@link Expectation} object associated with the identifier, or a
	 *         default object if not found.
	 */
	public Expectation get(String id) {
		return expectationMap.getOrDefault(id, new Expectation());
	}

	/**
	 * Retrieves all expectations stored in the cache.
	 * 
	 * @return A {@link Map} containing all expectations, keyed by their
	 *         identifiers.
	 */
	public Map<String, Expectation> get() {
		return expectationMap;
	}
}