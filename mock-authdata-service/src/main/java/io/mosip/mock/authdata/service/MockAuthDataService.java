package io.mosip.mock.authdata.service;

import io.mosip.mock.authdata.dto.MockAuthDataRequest;
import io.mosip.mock.authdata.exception.MockAuthDataException;

public interface MockAuthDataService {

	public void saveIdentity(MockAuthDataRequest mockAuthDataRequest) throws MockAuthDataException;
	
	public String getIdentity(String individualId) throws MockAuthDataException;
	
}
