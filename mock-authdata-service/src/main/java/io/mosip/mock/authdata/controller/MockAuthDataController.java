package io.mosip.mock.authdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.authdata.dto.MockAuthDataRequest;
import io.mosip.mock.authdata.dto.MockAuthDataResponse;
import io.mosip.mock.authdata.service.MockAuthDataService;

@CrossOrigin
@RestController
@RequestMapping("/")
public class MockAuthDataController {

	@Autowired
	private MockAuthDataService mockAuthDataService;

	@PostMapping(value = "createAuthdata", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public MockAuthDataResponse createMockAuthData(
			@RequestBody MockAuthDataRequest mockAuthDataRequest) {
		MockAuthDataResponse mockAuthDataResponse = new MockAuthDataResponse();
		try {
			mockAuthDataService.saveIdentity(mockAuthDataRequest);
			mockAuthDataResponse.setStatus("mock auth data created successfully");
		} catch (Exception e) {
			mockAuthDataResponse.setStatus("Error in creating  mock authdata" + e.getMessage());
		}

		return mockAuthDataResponse;

	}
}
