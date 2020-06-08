package io.mosip.mds.entitiy;

import io.mosip.mds.dto.*;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.service.TestRunnerService;
import io.mosip.mds.service.impl.TestRunnerServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Entity
@Data
@Table(name ="test_manager")
public class TestManager {

	@Id
	@Column(name = "run_id")
	public String runId;
	
	@Column(name = "mds_spec_version")
	public String mdsSpecVersion;
	
	
	public String process;

	@Column(name = "biometric_type")
	public String biometricType;

	@Column(name = "device_type")
	public String deviceType;

	public List<String> tests;

	//private static List<TestExtnDto> allTests = null;

	private static HashMap<String, TestExtnDto> allTests = null;

	private static Boolean isMasterDataLoaded = false;
	private static Boolean areTestsLoaded = false;

	private static MasterDataResponseDto masterData = null;

	private static HashMap<String, TestRun> testRuns = new HashMap<>();

	private static MasterDataResponseDto LoadMasterData()
	{
		// TODO add code here to load from file
		masterData = LoadMasterDataFromMemory();
		isMasterDataLoaded = true;
		return masterData;
	}

	private static void LoadTests()
	{
		// TODO Load Tests from file or db and comment out the below lines
		for(TestExtnDto test:LoadTestsFromMemory())
		{
			allTests.put(test.testId, test);
		}
		areTestsLoaded = true;
	}

	private static MasterDataResponseDto LoadMasterDataFromMemory()
	{
		// TODO load master data here
		return new MasterDataResponseDto();
	}

	private static TestExtnDto[] LoadTestsFromMemory()
	{
		// Add test 1
		List<TestExtnDto> memTests = new ArrayList<TestExtnDto>();
		TestExtnDto test1 = new TestExtnDto();
		memTests.add(test1);

		// Add test 2
		TestExtnDto test2 = new TestExtnDto();
		memTests.add(test2);

		// Add test 3
		TestExtnDto test3 = new TestExtnDto();
		memTests.add(test3);
		
		// Add test 4
		TestExtnDto test4 = new TestExtnDto();
		memTests.add(test4);

		// Add test 5
		TestExtnDto test5 = new TestExtnDto();
		memTests.add(test5);

		return memTests.toArray(new TestExtnDto[0]);
	}



	private static List<TestExtnDto> FilterTests(TestManagerGetDto filter)
	{
		List<TestExtnDto> results = new ArrayList<TestExtnDto>();

		for(TestExtnDto test:allTests.values())
		{
			// TODO see if forEach lambda expression can be used		
			if(test.processes.contains(filter.process)
				&& test.biometricTypes.contains(filter.biometricType)
				&& test.deviceTypes.contains(filter.deviceType)
				&& (test.mdsSpecVersions == null || test.mdsSpecVersions.isEmpty() || test.mdsSpecVersions.contains(filter.mdsSpecificationVersion))
			)
				results.add(test);
		}
		return results;	
	}

	private void SaveRun(RunExtnDto newRun)
	{
		// TODO save the Run to file as well as memory
		if(testRuns.keySet().contains(newRun.runId))
			return;
		TestRun newTestRun = new TestRun();
		newTestRun.runId = newRun.runId;
		newTestRun.createdOn = new Date();
		newTestRun.runStatus = RunStatus.Created;
		Collections.addAll(newTestRun.tests, newRun.tests);
		testRuns.put(newRun.runId, newTestRun);
	}

	public MasterDataResponseDto GetMasterData()
	{
		if (isMasterDataLoaded)
			return masterData;
		return LoadMasterData();
	}

	public TestExtnDto[] GetTests(TestManagerGetDto filter)
	{
		if (!areTestsLoaded)
			LoadTests();
		return FilterTests(filter).toArray(new TestExtnDto[0]);
	}

	public RunExtnDto CreateRun(TestManagerDto runInfo)
	{
		RunExtnDto newRun = new RunExtnDto();
		// Validate the tests given
		Boolean notFound = false;
		for(String testName:runInfo.tests)
		{
			if(!allTests.keySet().contains(testName))
			{
				notFound = true;
				break;
			}
		}
		if(notFound)
			return null;
		// Assign a Run Id
		newRun.runId = "" + System.currentTimeMillis();
		// Save the run details
		newRun.tests = runInfo.tests.toArray(new String[0]);
		SaveRun(newRun);
		return newRun;
	}

	public TestReport GetReport(String runId)
	{
		if(!testRuns.keySet().contains(runId))
			return null;
		return new TestReport(testRuns.get(runId)); 
	}

	private ComposeRequestResponseDto BuildRequest(ComposeRequestDto requestParams)
	{
		return null;
	}

	public ComposeRequestResponseDto ComposeRequest(ComposeRequestDto composeRequestDto) {

		// TODO create and use actual request composers
		TestRunnerService svc = new TestRunnerServiceImpl();
		TestExtnDto test = allTests.get(composeRequestDto.testId);
		
		return BuildRequest(composeRequestDto);
	}
}
