package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.entity.Expectation;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.IdentityResponse;
import io.mosip.proxy.abis.entity.InsertRequestMO;

import java.util.Map;

public interface ProxyAbisInsertService {
	

	
	public void deleteData(String referenceId);

	public void insertData(InsertRequestMO ie);
	
	public IdentityResponse findDuplication(IdentityRequest ir);
	
	public String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias,
			String password,String keystore) ;

	public Boolean getDuplicate();
	public void setDuplicate(Boolean d);

	public Map<String, Expectation> getExpectations();

	public void setExpectation(Expectation exp);

	public void deleteExpectation(String id);
}
