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

    public String runId;

    public RunStatus runStatus;

    public Date createdOn;

    public List<String> tests;

    public HashMap<String, TestResult> testReport;
    
}