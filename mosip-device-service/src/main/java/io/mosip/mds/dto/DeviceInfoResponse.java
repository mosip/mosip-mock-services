package io.mosip.mds.dto;

public class DeviceInfoResponse {

    public String callbackId;
    public String certification;
    public String deviceCode;
    public String deviceId;
    public String deviceStatus;
    public String digitalId;
    public DigitalId digitalIdDecoded;
    public String env;
    public MDSError error;
    public String firmware;
    public Integer[] deviceSubId;
    public String purpose;
    public String serviceVersion;
    public String[] specVersion;

    /*
	private String DeviceServiceId;
	private String DeviceInfoSignature;
	private String status;
	private String DeviceType;
	private String DeviceTypeName;
	private String DeviceProcessName;
	private String DeviceSubType;
	private String DeviceProviderName;
	private String VendorId;
	private String ProductId;
	private int[] deviceSubId;
    private String Make;
    private String Model;
    private String SerialNo;
    private String DeviceExpiryDate;
    private String DeviceTimestamp;
    private String ComplianceLevel;
    private String HostId;

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