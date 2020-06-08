package io.mosip.mds.entitiy;

import io.mosip.mds.dto.*;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.getresponse.BiometricTypeDto;
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

import ch.qos.logback.core.joran.conditional.ElseAction;
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

	private static HashMap<String, TestRun> testRuns = new HashMap<>();

	private static List<String> processList = new ArrayList<String>();

	private static List<BiometricTypeDto> biometricTypes = new ArrayList<BiometricTypeDto>();

	private static List<String> mdsSpecVersions = new ArrayList<String>();


	private static void LoadTests()
	{
		// TODO Load Tests from file or db and comment out the below lines
		for(TestExtnDto test:LoadTestsFromMemory())
		{
			allTests.put(test.testId, test);
		}
		areTestsLoaded = true;
	}

	private static void SetupMasterData()
	{
		// TODO load master data here from file
		if(!isMasterDataLoaded)
		{
			Collections.addAll(processList, "REGISTRATION", "AUTHENTICATION");
			Collections.addAll(mdsSpecVersions, "0.9.2","0.9.3", "0.9.4", "0.9.5");
			BiometricTypeDto finger = new BiometricTypeDto("FINGERPRINT");
			Collections.addAll(finger.deviceType, "SLAP", "SINGLE", "CAMERA");
			Collections.addAll(finger.segments, "LEFT SLAP", "RIGHT SLAP", "TWO THUMBS", "LEFT THUMB", "RIGHT THUMB",
			"LEFT INDEX", "RIGHT INDEX");
			BiometricTypeDto iris = new BiometricTypeDto("IRIS");
			Collections.addAll(iris.deviceType, "MONOCULAR", "BINOCULAR", "CAMERA");
			Collections.addAll(iris.segments, "FULL", "CROPPED");
			BiometricTypeDto face = new BiometricTypeDto("IRIS");
			Collections.addAll(face.deviceType, "STILL", "VIDEO");
			Collections.addAll(face.segments, "BUST", "HEAD");
			Collections.addAll(biometricTypes, finger, iris, face);
			isMasterDataLoaded = true;
		}
	}

	private static TestExtnDto[] LoadTestsFromMemory()
	{
		// Add test 1
		List<TestExtnDto> memTests = new ArrayList<TestExtnDto>();
		TestExtnDto test1 = new TestExtnDto();
		test1.testId = "discover";
		memTests.add(test1);

		// Add test 2
		TestExtnDto test2 = new TestExtnDto();
		test2.testId = "deviceinfo";
		memTests.add(test2);

		// Add test 3
		TestExtnDto test3 = new TestExtnDto();
		test3.testId = "capture";
		memTests.add(test3);
		
		// Add test 4
		TestExtnDto test4 = new TestExtnDto();
		test4.testId = "stream";
		memTests.add(test4);

		// Add test 5
		TestExtnDto test5 = new TestExtnDto();
		test5.testId = "rcapture";
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
		MasterDataResponseDto masterData = new MasterDataResponseDto();
		SetupMasterData();
		masterData.biometricType.addAll(biometricTypes);
		masterData.process.addAll(processList);
		masterData.mdsSpecificationVersion.addAll(mdsSpecVersions);
		return masterData;
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

	private TestRun[] FilterRuns(String email)
	{
		// TODO add filter code based on email
		return testRuns.values().toArray(new TestRun[0]);
	}

	public TestRun[] GetRuns(String email)
	{
		return FilterRuns(email);
	}

	private ComposeRequestResponseDto BuildRequest(ComposeRequestDto requestParams)
	{
		TestRunnerServiceImpl svc = new TestRunnerServiceImpl();
		//TestExtnDto test = allTests.get(requestParams.testId);

		if(requestParams.testId.contains("discover"))
		{
			return svc.composeDiscover(requestParams.testId, requestParams.deviceInfo.get(0));
		}
		else if(requestParams.testId.contains("deviceinfo"))
		{
			return svc.composeDeviceInfo(requestParams.testId, requestParams.deviceInfo.get(0));
		}
		else if(requestParams.testId.contains("capture"))
		{
			return svc.composeCapture(requestParams.testId, requestParams.deviceInfo.get(0));
		}
		else if(requestParams.testId.contains("stream"))
		{
			return svc.composeStream(requestParams.testId, requestParams.deviceInfo.get(0));
		}
		else if(requestParams.testId.contains("rcapture"))
		{
			return svc.composeRegistrationCapture(requestParams.testId, requestParams.deviceInfo.get(0));
		}
		else
		{
			return svc.composeDiscover(requestParams.testId, requestParams.deviceInfo.get(0));
		}
	}

	public ComposeRequestResponseDto ComposeRequest(ComposeRequestDto composeRequestDto) {

		// TODO create and use actual request composers		
		return BuildRequest(composeRequestDto);
	}
}
