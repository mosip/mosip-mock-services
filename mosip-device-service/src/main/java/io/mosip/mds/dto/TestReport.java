package io.mosip.mds.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TestReport {
        @Override
    public String toString() {
        return String.format(
                "\n Test Report"
                +"\n Run Id "+runId
                        +"\n Run Status " + runStatus
                        +"\n Created On " +createdOn
                        +"\n Tests \n" + tests.toString()
                        +"\n Test Report \n" + testReport

        );
    }

    public TestReport(TestRun testRun)
    {
        this.runId = testRun.runId;
        this.runStatus = testRun.runStatus.toString();
        this.createdOn = testRun.createdOn;
        this.tests.addAll(testRun.tests);
        this.testReport.putAll(testRun.testReport);
    }

    public String runId;

    public String runStatus;

    public Date createdOn;

    public List<String> tests = new ArrayList<String>();

    public HashMap<String, TestResult> testReport = new HashMap<>();

}
