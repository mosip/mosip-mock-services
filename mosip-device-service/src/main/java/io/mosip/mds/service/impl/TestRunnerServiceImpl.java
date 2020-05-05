package io.mosip.mds.service.impl;

import org.springframework.stereotype.Service;

import io.mosip.mds.dto.ComposeRequestDto;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.ValidateResponseRequestDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;
import io.mosip.mds.dto.postresponse.ValidateResponseDto;
import io.mosip.mds.service.TestRunnerService;

@Service
public class TestRunnerServiceImpl implements TestRunnerService {

	@Override
	public ComposeRequestResponseDto composeRequest(ComposeRequestDto composeRequestDto) {
		// TODO Auto-generated method stub
		//get test information bassed on test id
		// create private method discover
		
		return null;
	}
/**
 * get information for testId when use that what should go in body
 * 
 * @param testId
 * @param deviceDto
 * @return
 * 
 * 
 */
	private ComposeRequestResponseDto composeDiscover(String testId, DeviceDto deviceDto) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto();
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.setUrl("http://127.0.0.1:<device_service_port>/device");
		requestInfoDto.setVerb("MOSIPDISC");
		requestInfoDto.setBody("{\n" + 
				"  \"type\": \"type of the device\"\n" + 
				"}");
		composeRequestResponseDto.setRequestInfoDto(requestInfoDto);
		
		return composeRequestResponseDto;
	}
	private ComposeRequestResponseDto composeDeviceInfo(String testId) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto();
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.setUrl("http://127.0.0.1:<device_service_port>/device");
		requestInfoDto.setVerb("MOSIPDINFO");
		requestInfoDto.setBody("{\n" + 
				"  \"type\": \"type of the device\"\n" + 
				"}");
		composeRequestResponseDto.setRequestInfoDto(requestInfoDto);
		
		return composeRequestResponseDto;
	}
	private ComposeRequestResponseDto composeStream(String testId) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto();
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.setUrl("http://127.0.0.1:<device_service_port>/stream");
		requestInfoDto.setVerb("STREAM");
		requestInfoDto.setBody("{\n" + 
				"  \"deviceId\": \"internal Id\",\n" + 
				"  \"deviceSubId\": \"device sub Id’s\"\n" + 
				"}");
		composeRequestResponseDto.setRequestInfoDto(requestInfoDto);
		
		return composeRequestResponseDto;
	}
	private ComposeRequestResponseDto composeCapture(String testId) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto();
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.setUrl("http://127.0.0.1:<device_service_port>/capture");
		requestInfoDto.setVerb("CAPTURE");
		requestInfoDto.setBody("{\n" + 
				"  \"env\":  \"Target environment\",\n" + 
				"  \"purpose\": \"Auth  or Registration\",\n" + 
				"  \"specVersion\": \"Expected version of the MDS spec\",\n" + 
				"  \"timeout\" : <Timeout for capture>,\n" + 
				"  \"captureTime\": <Time of capture request in ISO format including timezone>,\n" + 
				"  \"domainUri\": <URI of the auth server>,\n" + 
				"  \"transactionId\": <Transaction Id for the current capture>,\n" + 
				"  \"bio\": [\n" + 
				"    {\n" + 
				"      \"type\": <type of the biometric data>,\n" + 
				"      \"count\":  <fingerprint/Iris count, in case of face max is set to 1>,\n" + 
				"      \"bioSubType\": [\"Array of subtypes\"],\n" + 
				"      \"requestedScore\": <expected quality score that should match to complete a successful capture>,\n" + 
				"      \"deviceId\": <internal Id>,\n" + 
				"      \"deviceSubId\": <specific device Id>,\n" + 
				"      \"previousHash\": <hash of the previous block>\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  customOpts: {\n" + 
				"    //max of 50 key value pair. This is so that vendor specific parameters can be sent if necessary. The values cannot be hard coded and have to be configured by the apps server and should be modifiable upon need by the applications. Vendors are free to include additional parameters and fine-tuning parameters. None of these values should go undocumented by the vendor. No sensitive data should be available in the customOpts.\n" + 
				"  }\n" + 
				"}");
		composeRequestResponseDto.setRequestInfoDto(requestInfoDto);
		
		return composeRequestResponseDto;
	}
	private ComposeRequestResponseDto composeRegistrationCapture(String testId) {
		ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto();
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		requestInfoDto.setUrl("http://127.0.0.1:<device_service_port>/capture");
		requestInfoDto.setVerb("RCAPTURE");
		requestInfoDto.setBody("{\n" + 
				"  \"env\":  \"target environment\",\n" + 
				"  \"specVersion\": \"expected MDS spec version\",\n" + 
				"  \"timeout\": \"timeout for registration capture\",\n" + 
				"  \"captureTime\": \"time of capture request in ISO format including timezone\",\n" + 
				"  \"registrationId\": \"registration Id for the current capture\",\n" + 
				"  \"bio\": [\n" + 
				"    {\n" + 
				"      \"type\": \"type of the biometric data\",\n" + 
				"      \"count\":  \"fingerprint/Iris count, in case of face max is set to 1\",\n" + 
				"      \"exception\": [\"finger or iris to be excluded\"],\n" + 
				"      \"requestedScore\": \"expected quality score that should match to complete a successful capture.\",\n" + 
				"      \"deviceId\": \"internal Id\",\n" + 
				"      \"deviceSubId\": \"specific device Id\",\n" + 
				"      \"previousHash\": \"hash of the previous block\"\n" + 
				"      }\n" + 
				"  ],\n" + 
				"  customOpts: {\n" + 
				"    //max of 50 key value pair. This is so that vendor specific parameters can be sent if necessary. The values cannot be hard coded and have to be configured by the apps server and should be modifiable upon need by the applications. Vendors are free to include additional parameters and fine-tuning parameters. None of these values should go undocumented by the vendor. No sensitive data should be available in the customOpts.\n" + 
				"  }\n" + 
				"}");
		composeRequestResponseDto.setRequestInfoDto(requestInfoDto);
		
		return composeRequestResponseDto;
	}
	
	public void getTest(String testId) {
		
	}
	
	
	
	
	@Override
	public ValidateResponseDto validateResponse(ValidateResponseRequestDto validateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

}
