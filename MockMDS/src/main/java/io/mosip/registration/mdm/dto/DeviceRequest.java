package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DeviceRequest {
	private String[] specVersion;
	private String env;
	private String digitalId;
	private String deviceId;
	private String deviceCode;
	private String purpose;
	private String serviceVersion;
	private String deviceStatus;
	private String firmware;
	private String certification;
	private String[] deviceSubId;
	private String callbackId;
}