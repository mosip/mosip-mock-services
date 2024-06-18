package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ProxyAbisConfigService} providing configuration and
 * management of ABIS (Automated Biometric Identification System) operations.
 * <p>
 * This service manages configuration settings related to duplicate biometric
 * data handling, cached expectations, and cached biometric data operations. It
 * interacts with repositories to fetch and manage biometric data and
 * expectations stored in persistent and memory-based caches.
 * </p>
 * <p>
 * Use this service to configure ABIS behavior, manage expectations for testing
 * or forced responses, and perform operations related to cached biometric data
 * retrieval and deletion.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Service
public class ProxyAbisConfigServiceImpl implements ProxyAbisConfigService {
	private ProxyAbisInsertRepository proxyabis;
	private ProxyAbisBioDataRepository proxyAbisBioDataRepository;
	private ExpectationCache expectationCache;

	/**
	 * Flag to determine if duplicate biometric data should be returned. Default
	 * value is {@code true}.
	 */
	@Value("${abis.return.duplicate:true}")
	private boolean findDuplicate;

	/**
	 * Flag to force returning duplicate biometric data regardless of configuration.
	 * Default value is {@code false}.
	 */
	@Value("${abis.force.return.duplicate:false}")
	private boolean forceDuplicate;

	/**
	 * Constructs an instance of {@code ProxyAbisConfigServiceImpl} with required
	 * repositories and caches.
	 * 
	 * @param proxyabis                  The repository for handling insert
	 *                                   operations.
	 * @param proxyAbisBioDataRepository The repository for managing biometric data.
	 * @param expectationCache           The cache for managing expectations.
	 */
	@Autowired
	public ProxyAbisConfigServiceImpl(ProxyAbisInsertRepository proxyabis,
			ProxyAbisBioDataRepository proxyAbisBioDataRepository, ExpectationCache expectationCache) {
		this.proxyabis = proxyabis;
		this.proxyAbisBioDataRepository = proxyAbisBioDataRepository;
		this.expectationCache = expectationCache;
	}

	/**
	 * Retrieves the current configuration setting for returning duplicate biometric
	 * data.
	 * 
	 * @return {@code true} if duplicate biometric data can be returned,
	 *         {@code false} otherwise.
	 */
	public Boolean getDuplicate() {
		return findDuplicate;
	}

	/**
	 * Sets the configuration for returning duplicate biometric data.
	 * 
	 * @param d The value to set for returning duplicate biometric data.
	 */
	public void setDuplicate(Boolean d) {
		findDuplicate = d;
	}

	/**
	 * Checks if forced returning of duplicate biometric data is enabled.
	 * 
	 * @return {@code true} if forced duplicate biometric data return is enabled,
	 *         {@code false} otherwise.
	 */
	public Boolean isForceDuplicate() {
		return forceDuplicate;
	}

	/**
	 * Retrieves all cached expectations.
	 * 
	 * @return A {@link Map} containing all expectations, keyed by their
	 *         identifiers.
	 */
	public Map<String, Expectation> getExpectations() {
		return expectationCache.get();
	}

	/**
	 * Inserts or updates an expectation in the cache.
	 * 
	 * @param exp The expectation to insert or update.
	 */
	public void setExpectation(Expectation exp) {
		expectationCache.insert(exp);
	}

	/**
	 * Deletes an expectation from the cache.
	 * 
	 * @param id The identifier of the expectation to delete.
	 */
	public void deleteExpectation(String id) {
		expectationCache.delete(id);
	}

	/**
	 * Deletes all expectations from the cache.
	 */
	public void deleteExpectations() {
		expectationCache.deleteAll();
	}

	/**
	 * Retrieves all cached biometric data.
	 * 
	 * @return A list of strings representing all cached biometric data.
	 */
	public List<String> getCachedBiometrics() {
		return proxyAbisBioDataRepository.fetchAllBioData();
	}

	/**
	 * Retrieves cached biometric data matching the given hash.
	 * 
	 * @param hash The hash value of the biometric data to retrieve.
	 * @return A list of strings representing cached biometric data matching the
	 *         hash.
	 */
	public List<String> getCachedBiometric(String hash) {
		return proxyAbisBioDataRepository.fetchByBioData(hash);
	}

	/**
	 * Deletes all cached biometric data from both the biometric and insert
	 * repositories.
	 */
	public void deleteAllCachedBiometrics() {
		proxyAbisBioDataRepository.deleteAll();
		proxyabis.deleteAll();
	}
}