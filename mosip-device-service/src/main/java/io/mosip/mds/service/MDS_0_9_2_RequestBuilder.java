package io.mosip.mds.service;

import io.mosip.mds.dto.CaptureRequest;
import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.DeviceInfoRequest;
import io.mosip.mds.dto.DiscoverRequest;
import io.mosip.mds.dto.RegistrationCaptureRequest;
import io.mosip.mds.dto.StreamRequest;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;
import io.mosip.mds.dto.postresponse.RequestInfoDto;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MDS_0_9_2_RequestBuilder implements IMDSRequestBuilder {

    public String GetSpecVersion()
    {
        return "0.9.2";
    }

    String defaultPort = "4501";

    private String GetPort(DeviceDto deviceDto)
	{
		return (deviceDto != null && deviceDto.port != null) ? deviceDto.port : defaultPort;
	}
    
    private ObjectMapper mapper = new ObjectMapper();

    public ComposeRequestResponseDto BuildRequest(TestRun run, TestExtnDto test, DeviceDto device, Intent op)
    {
        ComposeRequestResponseDto composeRequestResponseDto=new ComposeRequestResponseDto(run.runId, test.testId);
		RequestInfoDto requestInfoDto=new RequestInfoDto();
		
		
        try
        {
            switch(op)
            {
                case Discover:
                    requestInfoDto.verb = "MOSIPDISC";
                    requestInfoDto.body = Discover(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + GetPort(device) + "/device";
                    break;
                case Stream:
                    requestInfoDto.verb = "STREAM";
                    requestInfoDto.body = Stream(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + GetPort(device) + "/stream";
                break;
                case Capture:
                    requestInfoDto.verb = "CAPTURE";
                    requestInfoDto.body = Capture(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + GetPort(device) + "/capture";
                break;
                case RegistrationCapture:
                    requestInfoDto.verb = "RCAPTURE";
                    requestInfoDto.body = RegistrationCapture(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + GetPort(device) + "/capture";
                break;
                case DeviceInfo:
                    requestInfoDto.verb = "MOSIPDISC";
                    requestInfoDto.body = DeviceInfo(test, device);
                    requestInfoDto.url = "http://127.0.0.1:" + GetPort(device) + "/device";
        }
        }
        catch(JsonProcessingException jEx)
        {
        }
        composeRequestResponseDto.requestInfoDto = requestInfoDto;
        return composeRequestResponseDto;
    }

    private String Discover(TestExtnDto test, DeviceDto device) throws JsonProcessingException 
    {
        DiscoverRequest requestBody = new DiscoverRequest();
        requestBody.type = "BIOMETRIC DEVICE";
        return mapper.writeValueAsString(requestBody);
    }

    private String DeviceInfo(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        DeviceInfoRequest requestBody = new DeviceInfoRequest();
        return mapper.writeValueAsString(requestBody);
    }

    private String Stream(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        StreamRequest requestBody = new StreamRequest();
        requestBody.deviceId = device.discoverInfo;
        requestBody.deviceSubId = 1;
        // TODO extract discoverinfo into device dto
        return mapper.writeValueAsString(requestBody);
    }

    private String Capture(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        CaptureRequest requestBody = new CaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        requestBody.domainUri = "default";
        requestBody.env = "test";
        requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.2";
        requestBody.timeout = 30;
        requestBody.transactionId = "" + System.currentTimeMillis();
        CaptureRequest.CaptureBioRequest bio = requestBody.new CaptureBioRequest();
        bio.count = 1;
        bio.deviceId = device.discoverInfo;
        bio.deviceSubId = 1;
        bio.previousHash = "";
        bio.requestedScore = 80;
        bio.bioSubType = new String[]{"FULL"};
        bio.type = "FACE";
        requestBody.bio = new CaptureRequest.CaptureBioRequest[]{bio};
        return mapper.writeValueAsString(requestBody);
    }

    private String RegistrationCapture(TestExtnDto test, DeviceDto device) throws JsonProcessingException
    {
        RegistrationCaptureRequest requestBody = new RegistrationCaptureRequest();
        requestBody.captureTime = (new Date()).toString();
        //requestBody.domainUri = "default";
        requestBody.env = "test";
        //requestBody.purpose = "AUTHENTICATION";
        requestBody.specVersion = "0.9.2";
        requestBody.timeout = 30;
        requestBody.registrationId = "" + System.currentTimeMillis();
        RegistrationCaptureRequest.RegistrationCaptureBioRequest bio = requestBody.new RegistrationCaptureBioRequest();
        bio.count = 1;
        bio.deviceId = device.discoverInfo;
        bio.deviceSubId = 1;
        bio.previousHash = "";
        bio.requestedScore = 80;
        bio.exception = new String[]{};
        bio.type = "FACE";
        requestBody.bio = new RegistrationCaptureRequest.RegistrationCaptureBioRequest[]{bio};
        return mapper.writeValueAsString(requestBody);
    }

}