package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.dto.Expectation;

import java.util.List;
import java.util.Map;

/**
 * Service interface defining operations for managing ABIS (Automated Biometric
 * Identification System) configurations. This includes functionalities for
 * setting and getting configurations related to duplication checks,
 * expectations, and cached biometrics.
 */
public interface ProxyAbisConfigService {

	/**
	 * Retrieves the current configuration setting for checking duplicates.
	 *
	 * @return A {@link Boolean} indicating whether duplicate checking is enabled.
	 */
	public Boolean getDuplicate();

	/**
	 * Sets the configuration for checking duplicates.
	 *
	 * @param d A {@link Boolean} indicating whether duplicate checking should be
	 *          enabled or disabled.
	 */
	public void setDuplicate(Boolean d);

	/**
	 * Checks if the system is configured to force duplicate checking.
	 *
	 * @return A {@link Boolean} indicating whether force duplicate checking is
	 *         enabled.
	 */
	public Boolean isForceDuplicate();

	/**
	 * Retrieves the current expectations for the ABIS system.
	 *
	 * @return A {@link Map} where the keys are expectation identifiers and the
	 *         values are {@link Expectation} objects.
	 */
	public Map<String, Expectation> getExpectations();

	/**
	 * Sets a specific expectation in the ABIS system.
	 *
	 * @param exp An {@link Expectation} object representing the expectation to be
	 *            set.
	 */
	public void setExpectation(Expectation exp);

	/**
	 * Deletes a specific expectation from the ABIS system.
	 *
	 * @param id A {@link String} representing the identifier of the expectation to
	 *           be deleted.
	 */
	public void deleteExpectation(String id);

	/**
	 * Deletes all expectations from the ABIS system.
	 */
	public void deleteExpectations();

	/**
	 * Retrieves a list of cached biometric data hashes.
	 *
	 * @return A {@link List} of {@link String} representing the hashes of cached
	 *         biometric data.
	 */
	public List<String> getCachedBiometrics();

	/**
	 * Retrieves cached biometric data for a specific hash.
	 *
	 * @param hash A {@link String} representing the hash of the biometric data to
	 *             be retrieved.
	 * @return A {@link List} of {@link String} representing the cached biometric
	 *         data for the specified hash.
	 */
	public List<String> getCachedBiometric(String hash);

	/**
	 * Deletes all cached biometric data from the ABIS system.
	 */
	public void deleteAllCachedBiometrics();
}