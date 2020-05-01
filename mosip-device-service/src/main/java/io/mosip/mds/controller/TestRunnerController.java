package io.mosip.mds.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ValidateResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/testrunner")
@Api(tags = { "TestRunner" })
public class TestRunnerController {

	@PostMapping("/composerequest")
	@ApiOperation(value = "Service to save composeRequest", notes = "Saves composeRequest and json")
	@ApiResponses({ @ApiResponse(code = 201, message = "When composerequest Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating composerequest any error occured") })
	public void composeRequest(@RequestBody ComposeRequestDto composeRequestDto) {
		
	}
	@PostMapping("/validateresponse")
	@ApiOperation(value = "Service to save validateResponse", notes = "Saves validateResponse and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When validateResponse Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating validateResponse any error occured") })
	public ValidateResponseDto validateResponse(@RequestBody ValidateResponseRequestDto validateRequestDto) {
		return null;
		
		
	}
}
