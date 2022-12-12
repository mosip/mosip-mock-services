package io.mosip.mock.authentication.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.authentication.dto.MockAuthData;
import io.mosip.mock.authentication.dto.MockAuthDataResponse;
import io.mosip.mock.authentication.dto.RequestWrapper;
import io.mosip.mock.authentication.dto.ResponseWrapper;
import io.mosip.mock.authentication.exception.MockAuthDataException;
import io.mosip.mock.authentication.service.MockAuthDataService;
import io.mosip.mock.authentication.util.MockAuthDataUtil;

@CrossOrigin
@RestController
@RequestMapping("/")
public class MockAuthDataController {

	@Autowired
	private MockAuthDataService mockAuthDataService;

	@PostMapping(value = "mock-identity", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseWrapper<MockAuthDataResponse> createMockIdentity
	(@Valid @RequestBody RequestWrapper<MockAuthData> requestWrapper) throws MockAuthDataException {

		ResponseWrapper response = new ResponseWrapper<MockAuthDataResponse>();
		MockAuthDataResponse mockAuthDataResponse = new MockAuthDataResponse();
		mockAuthDataService.saveIdentity(requestWrapper.getRequest());
		mockAuthDataResponse.setStatus("mock auth data created successfully");
		response.setResponse(mockAuthDataResponse);
		response.setResponseTime(MockAuthDataUtil.getUTCDateTime());
		return response;
	}
	
	@GetMapping(value = "mock-identity")
	public ResponseWrapper<MockAuthData> getMockIdentity(@PathVariable("virtual-id") String virtualId) throws MockAuthDataException {
		ResponseWrapper<MockAuthData> response = new ResponseWrapper<>();
		response.setResponse(mockAuthDataService.getIdentity(virtualId));
		response.setResponseTime(MockAuthDataUtil.getUTCDateTime());
		return response;	
	}
}
