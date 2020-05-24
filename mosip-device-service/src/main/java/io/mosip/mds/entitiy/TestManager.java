package io.mosip.mds.entitiy;

import io.mosip.mds.dto.*;
import io.mosip.mds.dto.TestRun.RunStatus;
import io.mosip.mds.dto.getresponse.BiometricTypeDto;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.getresponse.UIInput;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RunExtnDto;
import io.mosip.mds.service.TestRequestBuilder;
import io.mosip.mds.service.TestRequestBuilder.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.Base64;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private static HashMap<String, TestExtnDto> allTests = new HashMap<>();

	private static Boolean isMasterDataLoaded = false;
	private static Boolean areTestsLoaded = false;
	private static Boolean areRunsLoaded = false;

	private static HashMap<String, TestRun> testRuns = new HashMap<>();

	private static List<String> processList = new ArrayList<String>();

	private static List<BiometricTypeDto> biometricTypes = new ArrayList<BiometricTypeDto>();

	private static List<String> mdsSpecVersions = new ArrayList<String>();

	public TestManager()
	{
		InitStaticData();
	}

	private static void InitStaticData()
	{
		SetupMasterData();
		LoadTests();
		LoadRuns();
	}

	private static void LoadRuns()
	{
		if(areRunsLoaded)
			return;
		List<String> users = Store.GetUsers();
		for(String user:users)
		{
			List<String> runIds = Store.GetRunIds(user);
			for(String runId:runIds)
			{
				TestRun run = Store.GetRun(user, runId);
				testRuns.put(runId, run);
			}
		}
		areRunsLoaded = true;
	}


	private static void LoadTests()
	{
		// TODO Load Tests from file or db and comment out the below lines
		if(!areTestsLoaded)
		{
			for(TestExtnDto test:LoadTestsFromMemory())
			{
				allTests.put(test.testId, test);
			}
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
			Collections.addAll(finger.deviceType, "SLAP", "FINGER", "CAMERA");
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
		test1.processes = Arrays.asList("REGISTRATION", "AUTHENTICATION");
		test1.biometricTypes = Arrays.asList("FINGERPRINT");
		test1.deviceTypes = Arrays.asList("SLAP", "FINGER");
		test1.uiInput = Arrays.asList(new UIInput("port","numeric"));
		test1.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test1);

		// Add test 2
		TestExtnDto test2 = new TestExtnDto();
		test2.testId = "deviceinfo";
		test2.processes = Arrays.asList("REGISTRATION");
		test2.biometricTypes = Arrays.asList("FINGERPRINT");
		test2.deviceTypes = Arrays.asList("SLAP");
		test2.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test2);

		// Add test 3
		TestExtnDto test3 = new TestExtnDto();
		test3.testId = "capture";
		test3.processes = Arrays.asList("REGISTRATION", "AUTHENTICATION");
		test3.biometricTypes = Arrays.asList("FINGERPRINT");
		test3.deviceTypes = Arrays.asList("SLAP", "FINGER");
		test3.validators = Arrays.asList(new CoinTossValidator());
		memTests.add(test3);
		
		// Add test 4
//		TestExtnDto test4 = new TestExtnDto();
//		test4.testId = "stream";
//		memTests.add(test4);
//
//		// Add test 5
//		TestExtnDto test5 = new TestExtnDto();
//		test5.testId = "rcapture";
//		memTests.add(test5);

		return memTests.toArray(new TestExtnDto[memTests.size()]);
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
		newTestRun.tests = new ArrayList<>();
		newTestRun.user = newRun.email;
		Collections.addAll(newTestRun.tests, newRun.tests);
		TestRun savedRun = PersistRun(newTestRun);
		testRuns.put(savedRun.runId, savedRun);
	}

	private TestRun PersistRun(TestRun run)
	{
		return Store.SaveTestRun(run.user, run);
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
		newRun.tests = runInfo.tests.toArray(new String[runInfo.tests.size()]);
		if(!runInfo.email.isEmpty())
			newRun.email = runInfo.email;
		else
		newRun.email = "misc";
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
		TestRequestBuilder builder = new TestRequestBuilder();
		
		TestRun run = testRuns.get(requestParams.runId);
		if(run == null || !run.tests.contains(requestParams.testId))
			return null;

		TestExtnDto test = allTests.get(requestParams.testId);
		Intent intent = Intent.Discover;

		if(requestParams.testId.contains("deviceinfo"))
		{
			intent = Intent.DeviceInfo;
		}
		else if(requestParams.testId.contains("rcapture"))
		{
			intent = Intent.RegistrationCapture;
		}
		else if(requestParams.testId.contains("capture"))
		{
			intent = Intent.Capture;
		}
		else if(requestParams.testId.contains("stream"))
		{
			intent = Intent.Stream;
		}

		return builder.BuildRequest(run, test, requestParams.deviceInfo, intent);
		
	}

	public ComposeRequestResponseDto ComposeRequest(ComposeRequestDto composeRequestDto) {

		// TODO create and use actual request composers		
		return BuildRequest(composeRequestDto);
	}

	public TestResult ValidateResponse(ValidateResponseRequestDto validateRequestDto) {
		if(!testRuns.keySet().contains(validateRequestDto.runId) || !allTests.keySet().contains(validateRequestDto.testId))
			return null;
		TestRun run = testRuns.get(validateRequestDto.runId);
		TestExtnDto test = allTests.get(validateRequestDto.testId);
		TestResult testResult = new TestResult();
		testResult.executedOn = new Date();
		testResult.requestData = validateRequestDto.mdsRequest;
		testResult.responseData = validateRequestDto.mdsResponse;
		testResult.runId = run.runId;
		testResult.testId = test.testId;

		for(Validator v:test.validators)
		{
			testResult.validationResults.add(v.Validate(validateRequestDto));
		}
		run.testReport.put(test.testId, testResult);
		PersistRun(run);
		return testResult; 
	}

	public DiscoverResponse DecodeDiscoverInfo(String discoverInfo) {
		DiscoverResponse response = new DiscoverResponse();

		// TODO add the decode code here

		return response;
	}

	public DeviceInfoResponse DecodeDeviceInfo(String deviceInfo) {
		DeviceInfoResponse response = new DeviceInfoResponse();
		// TODO add the decode code here
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		Pattern pattern = Pattern.compile("(?<=\\.)(.*)(?=\\.)");
		Matcher matcher = pattern.matcher(deviceInfo);
		String afterMatch = null;
		if (matcher.find()) {
			afterMatch = matcher.group(1);
		}
		try {
			String result = new String(
					Base64.getUrlDecoder().decode(new String(Base64.getUrlDecoder().decode(afterMatch)).getBytes()));
			response = (DeviceInfoResponse) (mapper.readValue(result.getBytes(), DeviceInfoResponse.class));
		} catch (Exception exception) {

			response.deviceStatus = exception.getMessage();

			// TODO Log the issue
		}	
		return response;
	}
}
