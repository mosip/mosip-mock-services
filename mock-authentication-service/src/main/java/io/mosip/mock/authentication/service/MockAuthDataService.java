package io.mosip.mock.authentication.service;

import io.mosip.mock.authentication.dto.MockAuthData;
import io.mosip.mock.authentication.exception.MockAuthDataException;

public interface MockAuthDataService {

	public void saveIdentity(MockAuthData mockAuthDataRequest) throws MockAuthDataException;
	
	public MockAuthData getIdentity(String individualId) throws MockAuthDataException;
	
}
