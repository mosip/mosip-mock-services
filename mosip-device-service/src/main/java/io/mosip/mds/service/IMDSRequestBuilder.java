package io.mosip.mds.service;

import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;
import io.mosip.mds.dto.postresponse.ComposeRequestResponseDto;

public interface IMDSRequestBuilder {
    public enum Intent
	{
		Discover,
		DeviceInfo,
		Capture,
		RegistrationCapture,
		Stream
    }

    public String GetSpecVersion();

    public ComposeRequestResponseDto BuildRequest(TestRun run, TestExtnDto test, DeviceDto device, Intent op);
   

}