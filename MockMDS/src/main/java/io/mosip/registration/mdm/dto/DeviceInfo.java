package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class DeviceInfo {
	public String[] specVersion;
    public String digitalId;
	public String deviceId;
	public String deviceCode;
	public String purpose;
	public String serviceVersion;
	public String DeviceServiceId;
	public String DeviceInfoSignature;
	public String status;
	public String deviceStatus;
	public String firmware;
	public String certification;
	public String DeviceType;
	public String DeviceTypeName;
	public String DeviceProcessName;
	public String DeviceSubType;
	public String DeviceProviderName;
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

}
