package io.mosip.mock.authdata.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

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
