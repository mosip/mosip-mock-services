package io.mosip.proxy.abis.service;

import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.IdentityResponse;
import io.mosip.proxy.abis.entity.InsertRequestMO;

public interface ProxyAbisInsertService {
	

	
	public void deleteData(String referenceId);

	public void insertData(InsertRequestMO ie);
	
	public IdentityResponse findDupication(IdentityRequest ir);
	
	public String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias,
			String password,String keystore) ;
}
