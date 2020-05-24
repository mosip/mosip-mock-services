package io.mosip.mds.controller;

import org.springframework.web.bind.annotation.*;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceInfoResponse;
import io.mosip.mds.dto.DiscoverResponse;
import io.mosip.mds.dto.TestResult;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.entitiy.TestManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@CrossOrigin("*")
@RestController
@RequestMapping("/testrunner")
@Api(tags = { "TestRunner" })
public class TestRunnerController {

	@PostMapping("/composerequest")
	@ApiOperation(value = "Service to save composeRequest", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When composerequest Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating composerequest any error occured") })
	public ComposeRequestResponseDto composeRequest(@RequestBody ComposeRequestDto composeRequestDto) {

		TestManager testManager = new TestManager();
		return testManager.ComposeRequest(composeRequestDto);

		
	}

	@PostMapping("/validateresponse")
	@ApiOperation(value = "Service to save validateResponse", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When validateResponse Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating validateResponse any error occured") })
	public TestResult validateResponse(@RequestBody ValidateResponseRequestDto validateRequestDto) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.ValidateResponse(validateRequestDto);	
	}

	@PostMapping("/decodediscover")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While processing discover data any error occured") })
	public DiscoverResponse decodeDiscover(@RequestBody String discoverInfo) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.DecodeDiscoverInfo(discoverInfo);	
	}

	@PostMapping("/decodedeviceinfo")
	@ApiOperation(value = "Service to extract discover info from ", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When decode is successful"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While processing device info data any error occured") })
	public DeviceInfoResponse decodeDeviceInfo(@RequestBody String deviceInfo) {
		// TODO handle null return for invalid runId and testId
		TestManager testManager = new TestManager();
		return testManager.DecodeDeviceInfo(deviceInfo);	
	}


}
