package io.mosip.mds.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TestRun {

    public enum RunStatus
    {
        Created,
        InProgress,
        Done
    };

    public String user;

    public String runId;

    public RunStatus runStatus;

    public Date createdOn;

    public String runName;

    public DeviceInfoResponse deviceInfo;

    public TestManagerDto targetProfile;

    public List<String> tests;

    public HashMap<String, TestResult> testReport = new HashMap<>();
    
}