package com.proxy.abis.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.proxy.abis.entity.InsertEntity;

@Repository
public interface ProxyAbisInsertRepository extends CrudRepository<InsertEntity, String>{
	
	
}
