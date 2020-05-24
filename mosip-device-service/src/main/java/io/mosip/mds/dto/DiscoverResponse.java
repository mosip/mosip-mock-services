package io.mosip.mds.dto;

public class DiscoverResponse {
 
    public String deviceId;

    public String deviceStatus;

    public String certification;

    public Integer[] deviceSubId;

    public String callbackId;

    public String deviceCode;

    public String specVersion;

    public String purpose;

    public MDSError error;

    public DigitalId digitalId;
    
    /*
    [
  {    
    "deviceId": "internal Id",
    "deviceStatus": "device status",
    "certification": "certification level",
    "serviceVersion": "device service version",
    "deviceSubId": "device sub Id’s",
    "callbackId": "baseurl to reach to the device",
    "digitalId": "digital id of the device",
    "deviceCode": "A unique code given by MOSIP after successful registration",
    "specVersion": ["Array of supported MDS specification version"],
    "purpose": "Auth  or Registration or empty if not registered",
    "error": {
        "errorcode": "101",
        "errorinfo": "Invalid JSON Value Type For Discovery.." 
        }
    },
    ...
]
    */
}