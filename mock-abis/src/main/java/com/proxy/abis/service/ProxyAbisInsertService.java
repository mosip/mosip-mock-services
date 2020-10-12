package com.proxy.abis.service;

import com.proxy.abis.entity.IdentityRequest;
import com.proxy.abis.entity.IdentityResponse;
import com.proxy.abis.entity.InsertRequestMO;

public interface ProxyAbisInsertService {
	

	
	public void deleteData(String referenceId);

	public void insertData(InsertRequestMO ie);
	
	public IdentityResponse findDupication(IdentityRequest ir);
}
