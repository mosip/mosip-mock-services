package io.mosip.mock.authdata.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mock.authdata.dto.MockAuthData;
import io.mosip.mock.authdata.entity.MockIdentity;
import io.mosip.mock.authdata.exception.MockAuthDataException;
import io.mosip.mock.authdata.repository.MockIdentityRepository;
import io.mosip.mock.authdata.service.MockAuthDataService;
import io.mosip.mock.authdata.util.ErrorConstants;

@Service
public class MockAuthDataServiceImpl implements MockAuthDataService {
	
	@Autowired
	ObjectMapper objectmapper;

	@Autowired
	MockIdentityRepository mockIdentityRepository;

	@Override
	public void saveIdentity(MockAuthData mockAuthDataRequest) throws MockAuthDataException {
		MockIdentity mockIdentity = new MockIdentity();
		try {
			mockIdentity.setIdentityJson(objectmapper.writeValueAsString(mockAuthDataRequest));
		} catch (JsonProcessingException e) {
			throw new MockAuthDataException(ErrorConstants.JSON_PROCESSING_ERROR);
		}
		mockIdentity.setIndividualId(mockAuthDataRequest.getVirtualId());
		mockIdentityRepository.save(mockIdentity);
	}

	@Override
	public MockAuthData getIdentity(String individualId) throws MockAuthDataException {
		Optional<MockIdentity> mockIdentity = mockIdentityRepository.findById(individualId);
		if (!mockIdentity.isPresent()) {
			throw new MockAuthDataException(ErrorConstants.INVALID_VIRTUAL_ID);
		}
		MockAuthData mockAuthData = new MockAuthData();
		try {
			mockAuthData = objectmapper.readValue(mockIdentity.get().getIdentityJson(), MockAuthData.class);
		} catch (JsonProcessingException e) {
			throw new MockAuthDataException(ErrorConstants.JSON_PROCESSING_ERROR);
		}
		return mockAuthData;
	}

}
