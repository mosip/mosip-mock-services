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
	public String deviceStatus;
	public String firmware;
	public String certification;
	public int[] deviceSubId;
	public String callbackId;
}
