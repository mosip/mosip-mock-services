package io.mosip.proxy.abis.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.proxy.abis.entity.BiometricData;

/**
 * Repository interface for managing CRUD operations and custom queries related
 * to {@link BiometricData}. Provides methods to retrieve, store, update, and
 * delete instances of {@link BiometricData} in the database.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * List<String> referenceIds = Arrays.asList("id1", "id2", "id3");
 * List<BiometricData> duplicates = proxyAbisBioDataRepository.fetchDuplicatesForReferenceId("referenceId");
 * }</pre>
 * </p>
 * 
 * <p>
 * The repository includes custom queries to fetch distinct biometric data
 * records and reference IDs based on various conditions:
 * </p>
 * <ul>
 * <li>{@link #fetchDuplicatesForReferenceId(String)}: Fetches duplicates of
 * biometric data for a given reference ID.</li>
 * <li>{@link #fetchDuplicatesForReferenceIdBasedOnGalleryIds(String, List)}:
 * Fetches duplicates of biometric data for a given reference ID within
 * specified gallery IDs.</li>
 * <li>{@link #fetchBioDataByRefId(String)}: Fetches biometric data by reference
 * ID.</li>
 * <li>{@link #fetchReferenceId(String)}: Fetches distinct reference IDs by
 * biometric data.</li>
 * <li>{@link #fetchAllBioData()}: Fetches all distinct biometric data.</li>
 * <li>{@link #fetchByBioData(String)}: Fetches biometric data by specific
 * biometric data value.</li>
 * <li>{@link #fetchByReferenceId(String, List)}: Fetches reference IDs by
 * biometric data within specified reference IDs.</li>
 * </ul>
 * 
 * <p>
 * The queries utilize native SQL for some operations for performance reasons.
 * </p>
 * 
 * <p>
 * Note: This repository extends {@link CrudRepository} which provides basic
 * CRUD operations for {@link BiometricData}.
 * </p>
 * 
 * @since 1.0.0
 */

@Repository
public interface ProxyAbisBioDataRepository extends CrudRepository<BiometricData, Long> {

	/**
	 * Fetches duplicates of biometric data for a given reference ID.
	 * 
	 * @param referenceId The reference ID to search for duplicates
	 * @return List of {@link BiometricData} instances representing duplicates
	 */
	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceId(@Param("referenceId") String referecenId);

	/**
	 * Fetches duplicates of biometric data for a given reference ID within
	 * specified gallery IDs.
	 * 
	 * @param referenceId  The reference ID to search for duplicates
	 * @param referenceIds List of gallery IDs to limit the search within
	 * @return List of {@link BiometricData} instances representing duplicates
	 */
	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  and b.reference_id in ?2 order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceIdBasedOnGalleryIds(@Param("referenceId") String referecenId,
			@Param("referenceIds") List<String> referenceIds);

	/**
	 * Fetches biometric data by reference ID.
	 * 
	 * @param referenceId The reference ID to search for biometric data
	 * @return List of biometric data values associated with the reference ID
	 */
	@Query(value = "select b.bioData from Biometric_Data b where b.insertEntity.referenceId=?1")
	public List<String> fetchBioDataByRefId(@Param("referenceId") String referenceId);

	/**
	 * Fetches distinct reference IDs by biometric data.
	 * 
	 * @param bioData The biometric data value to search for reference IDs
	 * @return List of distinct reference IDs associated with the biometric data
	 */
	@Query(value = "select distinct(b.insertEntity.referenceId) from Biometric_Data b where b.bioData=?1")
	public List<String> fetchReferenceId(@Param("bioData") String bioData);

	/**
	 * Fetches all distinct biometric data values.
	 * 
	 * @return List of all distinct biometric data values
	 */
	@Query(value = "select distinct(b.bio_data) from Biometric_Data b", nativeQuery = true)
	public List<String> fetchAllBioData();

	/**
	 * Fetches biometric data by specific biometric data value.
	 * 
	 * @param bioData The biometric data value to search for
	 * @return List of biometric data values matching the specified biometric data
	 *         value
	 */
	@Query(value = "select distinct(b.bio_data) from Biometric_Data b where b.bio_data=?1", nativeQuery = true)
	public List<String> fetchByBioData(@Param("bioData") String bioData);

	/**
	 * Fetches reference IDs by biometric data within specified reference IDs.
	 * 
	 * @param bioData      The biometric data value to search for
	 * @param referenceIds List of reference IDs to limit the search within
	 * @return List of reference IDs associated with the specified biometric data
	 *         within the given reference IDs
	 */
	@Query(value = "select distinct(b.insertEntity.referenceId) from Biometric_Data b where b.bioData=?1 and b.insertEntity.referenceId in ?2")
	public List<String> fetchByReferenceId(@Param("bioData") String bioData,
			@Param("referenceIds") List<String> referenceIds);
}