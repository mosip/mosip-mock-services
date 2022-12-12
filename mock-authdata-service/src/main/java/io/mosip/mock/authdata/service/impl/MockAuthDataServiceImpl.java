package io.mosip.mock.authdata.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mock.authdata.dto.MockAuthDataRequest;
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
	public void saveIdentity(MockAuthDataRequest mockAuthDataRequest) throws MockAuthDataException {
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
	public String getIdentity(String individualId) throws MockAuthDataException {
		Optional<MockIdentity> mockIdentity = mockIdentityRepository.findById(individualId);
		if (!mockIdentity.isPresent()) {
			//
		}
		return mockIdentity.get().getIdentityJson();
	}

}
