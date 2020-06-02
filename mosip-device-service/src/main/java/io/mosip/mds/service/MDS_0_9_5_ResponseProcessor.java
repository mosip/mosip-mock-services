package io.mosip.mds.service;

import io.mosip.mds.dto.DeviceDto;
import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.TestExtnDto;

public class MDS_0_9_5_ResponseProcessor implements IMDSResponseProcessor {

    @Override
    public String GetSpecVersion() {
        return "0.9.5";
    }

    @Override
    public String ProcessResponse(TestRun run, TestExtnDto test, DeviceDto device, Intent op) {
        switch(op)
        {
            case Capture:
                return ProcessCapture(run, test, device);
            case DeviceInfo:
                return ProcessDeviceInfo(run, test, device);
            case Discover:
                return ProcessDiscover(run, test, device);
            case RegistrationCapture:
                return ProcessRegistrationCapture(run, test, device);
            case Stream:
                return ProcessStream(run, test, device);
            default:
                return "";
        }
    }

    private String ProcessCapture(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String ProcessDiscover(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String ProcessDeviceInfo(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String ProcessStream(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }

    private String ProcessRegistrationCapture(TestRun run, TestExtnDto test, DeviceDto device)
    {
        return "";
    }
    
}