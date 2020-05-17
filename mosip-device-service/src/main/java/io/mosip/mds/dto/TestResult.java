package io.mosip.mds.dto;

import java.util.Date;
import java.util.List;

public class TestResult {
    
    public String runId;

    public String testId;

    public Date executedOn;
    
    public String requestData;

    public String responseData;

    public List<ValidationResult> validationResults;

}