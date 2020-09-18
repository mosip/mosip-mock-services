package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DeviceRequest {
	public String[] specVersion;
	public String env;
    public String digitalId;
	public String deviceId;
	public String deviceCode;
	public String purpose;
	public String serviceVersion;
	public String DeviceServiceId;
	public String DeviceInfoSignature;
	public String DeviceType;
	public String DeviceTypeName;
	public String DeviceProcessName;
	public String DeviceSubType;
	public String DeviceProviderName;
	public String status;
	public String deviceStatus;
	public String firmware;
	public String certification;
	public String VendorId;
	public String ProductId;
	public int[] deviceSubId;
	public String callbackId;
    public String Make;
    public String Model;
    public String SerialNo;
    public String DeviceExpiryDate;
    public String DeviceTimestamp;
    public String ComplianceLevel;
    public String HostId;
    public String ErrorId;
    public String Otp;

}
