package io.mosip.proxy.abis.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.proxy.abis.entity.InsertEntity;

/**
 * Repository interface for managing CRUD operations and custom queries related
 * to {@link InsertEntity}. Provides methods to retrieve, store, update, and
 * delete instances of {@link InsertEntity} in the database.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * List<String> referenceIds = Arrays.asList("id1", "id2", "id3");
 * int count = proxyAbisInsertRepository.fetchCountForReferenceIdPresentInGallery(referenceIds);
 * }</pre>
 * </p>
 * 
 * <p>
 * The repository includes a custom query to fetch the count of records based on
 * a list of reference IDs present in the database table named
 * {@code INSERT_REQUEST}. The native SQL query retrieves the count of records
 * where the {@code reference_id} matches any of the provided reference IDs.
 * </p>
 * 
 * <p>
 * Note: This repository extends {@link CrudRepository} which provides basic
 * CRUD operations for {@link InsertEntity}.
 * </p>
 * 
 * @since 1.0.0
 */
@Repository
public interface ProxyAbisInsertRepository extends CrudRepository<InsertEntity, String> {

	/**
	 * Custom query to fetch the count of records for given reference IDs present in
	 * the database.
	 * 
	 * @param referenceIds The list of reference IDs to search for
	 * @return The count of records matching the provided reference IDs
	 */
	@Query(value = "select count(b.reference_id) from INSERT_REQUEST b where b.reference_id in ?1", nativeQuery = true)
	public int fetchCountForReferenceIdPresentInGallery(@Param("referenceIds") List<String> referenceIds);
}