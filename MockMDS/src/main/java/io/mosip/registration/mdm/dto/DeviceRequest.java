package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DeviceRequest {
	private String[] specVersion;
    private String digitalId;
	private String deviceId;
	private String deviceCode;
	private String purpose;
	private String serviceVersion;
	private String DeviceServiceId;
	private String DeviceInfoSignature;
	private String DeviceType;
	private String DeviceTypeName;
	private String DeviceProcessName;
	private String DeviceSubType;
	private String DeviceProviderName;
	private String status;
	private String deviceStatus;
	private String firmware;
	private String certification;
	private String VendorId;
	private String ProductId;
	private int[] deviceSubId;
	private String callbackId;
    private String Make;
    private String Model;
    private String SerialNo;
    private String DeviceExpiryDate;
    private String DeviceTimestamp;
    private String ComplianceLevel;
    private String HostId;
    private String ErrorId;
    private String Otp;

}
