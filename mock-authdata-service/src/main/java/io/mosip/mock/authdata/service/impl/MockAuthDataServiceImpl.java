package io.mosip.mock.authdata.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mock.authdata.dto.Identity;
import io.mosip.mock.authdata.dto.MockAuthData;
import io.mosip.mock.authdata.dto.MockAuthDataRequest;
import io.mosip.mock.authdata.entity.MockIdentity;
import io.mosip.mock.authdata.repository.MockIdentityRepository;
import io.mosip.mock.authdata.service.MockAuthDataService;

@Service
public class MockAuthDataServiceImpl implements MockAuthDataService {
	
	@Autowired
	ObjectMapper objectmapper;

	@Autowired
	MockIdentityRepository mockIdentityRepository;

	@Override
	public void saveIdentity(MockAuthDataRequest mockAuthDataRequest) throws Exception {
		MockAuthData mockAuthData = new MockAuthData();
		mockAuthData.setPin(mockAuthDataRequest.getPin());
		mockAuthData.setIdentity(mapRequestToIdentity(mockAuthDataRequest));
		MockIdentity mockIdentity = new MockIdentity();
		mockIdentity.setIdentityJson(objectmapper.writeValueAsString(mockAuthData));
		mockIdentity.setIndividualId(mockAuthDataRequest.getIndividualId());
		mockIdentityRepository.save(mockIdentity);
	}

	private Identity mapRequestToIdentity(MockAuthDataRequest mockAuthDataRequest) {
		Identity identity = new Identity();
		identity.setFullName(mockAuthDataRequest.getFullName());
		identity.setDateOfBirth(mockAuthDataRequest.getDateOfBirth());
		identity.setGender(mockAuthDataRequest.getGender());
		identity.setPhone(mockAuthDataRequest.getPhone());
		identity.setEmail(mockAuthDataRequest.getEmail());
		identity.setAddressLine1(mockAuthDataRequest.getAddressLine1());
		identity.setAddressLine2(mockAuthDataRequest.getAddressLine2());
		identity.setAddressLine3(mockAuthDataRequest.getAddressLine3());
		identity.setPostal_code(mockAuthDataRequest.getPostal_code());
		identity.setProvince(mockAuthDataRequest.getProvince());
		identity.setRegion(mockAuthDataRequest.getRegion());
		identity.setZone(mockAuthDataRequest.getZone());
		identity.setEncodedPhoto(mockAuthDataRequest.getEncodedPhoto());
		return identity;
	}

	@Override
	public String getIdentity(String individualId) {
		Optional<MockIdentity> mockIdentity = mockIdentityRepository.findById(individualId);
		if (!mockIdentity.isPresent()) {
			//
		}
		return mockIdentity.get().getIdentityJson();
	}

}
