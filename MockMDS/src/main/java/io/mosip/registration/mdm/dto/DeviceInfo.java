package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class DeviceInfo {
	private String[] specVersion;
    private String digitalId;
	private String deviceId;
	private String deviceCode;
	private String purpose;
	private String serviceVersion;
	private String DeviceServiceId;
	private String DeviceInfoSignature;
	private String status;
	private String deviceStatus;
	private String firmware;
	private String certification;
	private String DeviceType;
	private String DeviceTypeName;
	private String DeviceProcessName;
	private String DeviceSubType;
	private String DeviceProviderName;
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

}
