package io.mosip.mds.controller;

import io.mosip.mds.entitiy.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mds.dto.TestManagerDto;
import io.mosip.mds.dto.TestManagerGetDto;
import io.mosip.mds.dto.TestReport;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "TestManager" })
@RequestMapping("/testmanager")
public class TestManagerController {

	private TestManager testManager = new TestManager();

	@GetMapping("/masterdata")
	@ApiOperation(value = "Retrieve all MasterData", notes = "Retrieve all MasterData")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When MasterData retrieved from database"),
			@ApiResponse(code = 404, message = "When No MasterData found"),
			@ApiResponse(code = 500, message = "While retrieving MasterData any error occured") })
	public MasterDataResponseDto getMasterData() {
		// TODO Add try catch here to handle 500 cases
		return testManager.GetMasterData();			
	}

	@PostMapping("/test")
	@ApiOperation(value = "Retrieve Test bassed on testManagerGetDto", notes = "Retrieve Test bassed on testManagerGetDto")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Test retrieved"),
			@ApiResponse(code = 404, message = "When No Test found"),
			@ApiResponse(code = 500, message = "While retrieving Test any error occured") })
	public TestExtnDto[] getTest(@RequestBody TestManagerGetDto testManagerGetDto) {
		// TODO Add try catch to handle 404 and 500 cases.
		return testManager.GetTests(testManagerGetDto);
	}

	@GetMapping("/runs/{email}")
	@ApiOperation(value = "Retrieve Test bassed on testManagerGetDto", notes = "Retrieve Test bassed on testManagerGetDto")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Test retrieved"),
			@ApiResponse(code = 404, message = "When No Test found"),
			@ApiResponse(code = 500, message = "While retrieving Test any error occured") })
	public TestRun[] getRuns(@PathVariable("email")String email) {
		// TODO Add try catch to handle 404 and 500 cases 
		return testManager.GetRuns(email);
	}

	
	@PostMapping("/createrun")
	@ApiOperation(value = "Service to save test Details", notes = "Saves test Details and return run id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When test Details successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating test any error occured") })
	public RunExtnDto createRun(@RequestBody TestManagerDto testManagerDto) {
	
		// TODO Add try catch tp handle 500 cases
		return testManager.CreateRun(testManagerDto);
		
	}
	
	@GetMapping("/report/{runId}/{format}")
	@ApiOperation(value = "Retrieve Test Report bassed on runId and format", notes = "Retrieve Test Report bassed on runId and format")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Test Report retrieved"),
			@ApiResponse(code = 404, message = "When Test Report found"),
			@ApiResponse(code = 500, message = "While retrieving Test Report any error occured") })
	public TestReport getTestReport(@PathVariable("runId")String runId, @PathVariable String format) {
		// TODO Add try catch as well as return handler for 404 and 500 cases
		return testManager.GetReport(runId);
	}
}
