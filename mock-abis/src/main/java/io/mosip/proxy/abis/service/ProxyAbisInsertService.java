package io.mosip.proxy.abis.service;

import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;

public interface ProxyAbisInsertService {
	public void deleteData(String referenceId);
	public int insertData(InsertRequestMO ie);
	public IdentifyDelayResponse findDuplication(IdentityRequest ir);
	public String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias, String password,
			String keystore);
}
