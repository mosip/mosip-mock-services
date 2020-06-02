package io.mosip.mds.service;

import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;

public interface IMDSResponseProcessor {
    public enum Intent
	{
		Discover,
		DeviceInfo,
		Capture,
		RegistrationCapture,
		Stream
    }

    public String GetSpecVersion();

    public String ProcessResponse(TestRun run, TestExtnDto test, DeviceDto device, Intent op);

}