package io.mosip.mds.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.mosip.mds.dto.postresponse.ValidationResult;

public class TestResult {
    
    public String runId;

    public String testId;

    public Date executedOn;

    public String summary;
    
    public String requestData;

    public String responseData;

    public List<ValidationResult> validationResults = new ArrayList<>();

}