package io.mosip.mds.dto;

import java.util.HashMap;

public class CaptureRequest {

    public class CaptureBioRequest
    {
        public String type; //type of the biometric data,
        public Integer count; //fingerprint/Iris count, in case of face max is set to 1
        public String[] bioSubType; //finger or iris to be excluded
        public Integer requestedScore; //expected quality score that should match to complete a successful capture
        public String deviceId; //internal Id
        public Integer deviceSubId; //specific device Id
        public String previousHash; //hash of the previous block
    };
    
    public String env; //target environment
    public String purpose; //Auth or Registration
    public String specVersion; //expected MDS spec version
    public Integer timeout; //timeout for registration capture
    public String captureTime; //time of capture request in ISO format including timezone
    public String domainUri; //URI of the auth server
    public String transactionId; //Transaction Id for the current capture
    public CaptureBioRequest[] bio;
    public HashMap<String, String> customOpts; //max of 50 key value pair. This is so that vendor specific parameters can be sent if necessary. The values cannot be hard coded and have to be configured by the apps server and should be modifiable upon need by the applications. Vendors are free to include additional parameters and fine-tuning parameters. None of these values should go undocumented by the vendor. No sensitive data should be available in the customOpts.
    
}