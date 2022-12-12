package io.mosip.mock.authdata.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.authdata.dto.MockAuthDataRequest;
import io.mosip.mock.authdata.dto.MockAuthDataResponse;
import io.mosip.mock.authdata.dto.RequestWrapper;
import io.mosip.mock.authdata.dto.ResponseWrapper;
import io.mosip.mock.authdata.exception.MockAuthDataException;
import io.mosip.mock.authdata.service.MockAuthDataService;
import io.mosip.mock.authdata.util.MockAuthDataUtil;

@CrossOrigin
@RestController
@RequestMapping("/")
public class MockAuthDataController {

	@Autowired
	private MockAuthDataService mockAuthDataService;

	@PostMapping(value = "mock-identity", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseWrapper<MockAuthDataResponse> createMockIdentity
	(@Valid @RequestBody RequestWrapper<MockAuthDataRequest> requestWrapper) throws MockAuthDataException {

		ResponseWrapper response = new ResponseWrapper<MockAuthDataResponse>();
		MockAuthDataResponse mockAuthDataResponse = new MockAuthDataResponse();
		mockAuthDataService.saveIdentity(requestWrapper.getRequest());
		mockAuthDataResponse.setStatus("mock auth data created successfully");
		response.setResponse(mockAuthDataResponse);
		response.setResponseTime(MockAuthDataUtil.getUTCDateTime());
		return response;
	}
	
	@GetMapping(value = "getIdentity")
	public ResponseEntity<Resource> getIdentity(@RequestParam("individualId") String individualId) throws IOException {
		String jsonContent = mockAuthDataService.getIdentity(individualId);
		
		File file = new File(individualId + ".json");
		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(jsonContent);
		}
		
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		
		final HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");
	    
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(resource.contentLength())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
}
