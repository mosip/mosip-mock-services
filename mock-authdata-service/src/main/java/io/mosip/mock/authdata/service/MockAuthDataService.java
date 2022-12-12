package io.mosip.mock.authdata.service;

import io.mosip.mock.authdata.dto.MockAuthData;
import io.mosip.mock.authdata.exception.MockAuthDataException;

public interface MockAuthDataService {

	public void saveIdentity(MockAuthData mockAuthDataRequest) throws MockAuthDataException;
	
	public MockAuthData getIdentity(String individualId) throws MockAuthDataException;
	
}
