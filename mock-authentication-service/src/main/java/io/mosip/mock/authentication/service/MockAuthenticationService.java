package io.mosip.mock.authentication.service;

import io.mosip.mock.authentication.dto.MockAuthData;
import io.mosip.mock.authentication.exception.MockAuthenticationException;

public interface MockAuthenticationService {

	public void saveIdentity(MockAuthData mockAuthDataRequest) throws MockAuthenticationException;
	
	public MockAuthData getIdentity(String individualId) throws MockAuthenticationException;
	
}
