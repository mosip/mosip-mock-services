package io.mosip.mds.dto;

public class DeviceInfoResponse {

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
    
    public String firmware;

    public String serviceVersion;

    public String env;

    /*

    [
  {
    "deviceInfo": { 
      "deviceStatus": "current status",
      "deviceId": "internal Id",
      "firmware": "firmware version",
      "certification": "certification level",
      "serviceVersion": "device service version",
      "deviceSubId": "device sub Id’s",
      "callbackId": "baseurl to reach to the device",
      "digitalId": "signed digital id as described in the digital id section of this document",
      "deviceCode": "A unique code given by MOSIP after successful registration",
      "env": "target environment",
      "purpose": "Auth  or Registration",
      "specVersion": ["Array of supported MDS specification version"],
    },
    "error": {
      "errorcode": "101",
      "errorinfo": "Invalid JSON Value "
    }
  }
  ...
]

    */

}