package com.proxy.abis.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proxy.abis.entity.BiometricData;

@Repository
public interface ProxyAbisBioDataRepository extends CrudRepository<BiometricData, Long> {

	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceId(@Param("referenceId") String referecenId);

	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  and b.reference_id in ?2 order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceIdBasedOnGalleryIds(@Param("referenceId") String referecenId,@Param("referenceIds") List<String> referenceIds);

}
