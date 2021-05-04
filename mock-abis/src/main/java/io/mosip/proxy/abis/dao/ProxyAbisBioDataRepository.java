package io.mosip.proxy.abis.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.proxy.abis.entity.BiometricData;

@Repository
public interface ProxyAbisBioDataRepository extends CrudRepository<BiometricData, Long> {

	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceId(@Param("referenceId") String referecenId);

	@Query(value = "select distinct(b.*) from Biometric_Data b where b.bio_data in (select bio_data from Biometric_Data where Biometric_Data.reference_id =?1) and b.reference_id <> ?1  and b.reference_id in ?2 order by reference_id asc", nativeQuery = true)
	public List<BiometricData> fetchDuplicatesForReferenceIdBasedOnGalleryIds(@Param("referenceId") String referenceId,@Param("referenceIds") List<String> referenceIds);

	@Query(value = "select bio_data from Biometric_data where reference_id=?1")
	public List<String> fetchBiodata(@Param("referenceId") String referenceId);
}
