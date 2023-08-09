package io.mosip.proxy.abis.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.proxy.abis.entity.InsertEntity;

@Repository
public interface ProxyAbisInsertRepository extends CrudRepository<InsertEntity, String> {

	@Query(value = "select count(b.reference_id) from INSERT_REQUEST b where b.reference_id in ?1", nativeQuery = true)
	public int fetchCountForReferenceIdPresentInGallery(@Param("referenceIds") List<String> referenceIds);
}