package io.mosip.mock.authdata.service;

import io.mosip.mock.authdata.dto.MockAuthDataRequest;

public interface MockAuthDataService {

	public void saveIdentity(MockAuthDataRequest mockAuthDataRequest) throws Exception;
	
	public void getIdentity();
	
}
