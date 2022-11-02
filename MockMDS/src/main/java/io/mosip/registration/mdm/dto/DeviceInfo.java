package io.mosip.registration.mdm.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class DeviceInfo {
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
	public String[] deviceSubId;
	public String callbackId;
}
