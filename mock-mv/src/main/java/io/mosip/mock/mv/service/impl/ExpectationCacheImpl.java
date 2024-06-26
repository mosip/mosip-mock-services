package io.mosip.mock.mv.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;

/**
 * Implementation of the {@link ExpectationCache} interface using a
 * ConcurrentHashMap for thread-safe operations.
 * <p>
 * This class is annotated with {@link Component} to indicate that it is a
 * Spring-managed bean.
 * </p>
 * 
 * @see ExpectationCache
 * @see Expectation
 */
@Component
public class ExpectationCacheImpl implements ExpectationCache {
	private Map<String, Expectation> expectationMap = new ConcurrentHashMap<>();

	/**
	 * Deletes an expectation from the cache based on the provided RId.
	 * 
	 * @param rid the RId of the expectation to be deleted.
	 * @return {@code true} if the expectation was found and deleted, {@code false}
	 *         otherwise.
	 */
	public boolean delete(String rid) {
		return expectationMap.remove(rid) != null;
	}

	/**
	 * Deletes all expectations from the cache.
	 */
	public void deleteAll() {
		expectationMap = new ConcurrentHashMap<>();
	}

	/**
	 * Inserts a new expectation into the cache.
	 * 
	 * @param expectation the {@link Expectation} object to be inserted.
	 */
	public void insert(Expectation expectation) {
		expectationMap.put(expectation.getRId(), expectation);
	}

	/**
	 * Retrieves all expectations currently stored in the cache.
	 * 
	 * @return a map of RId to {@link Expectation} objects.
	 */
	public Map<String, Expectation> get() {
		return expectationMap;
	}

	/**
	 * Retrieves an expectation from the cache based on the provided RId. If the RId
	 * is not found, returns a new {@link Expectation} object.
	 * 
	 * @param rid the RId of the expectation to be retrieved.
	 * @return the {@link Expectation} object associated with the provided RId, or a
	 *         new {@link Expectation} if not found.
	 */
	@Override
	public Expectation get(String rid) {
		return expectationMap.getOrDefault(rid, new Expectation());

	}
}