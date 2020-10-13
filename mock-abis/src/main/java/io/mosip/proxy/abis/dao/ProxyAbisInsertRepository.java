package io.mosip.proxy.abis.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.mosip.proxy.abis.entity.InsertEntity;

@Repository
public interface ProxyAbisInsertRepository extends CrudRepository<InsertEntity, String>{
	
	
}
