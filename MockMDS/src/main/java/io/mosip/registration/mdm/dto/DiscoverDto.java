package io.mosip.registration.mdm.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscoverDto {
	public String deviceId;
	public String deviceStatus;
	public String certification;
	public String serviceVersion;
	public String callbackId;
	public String digitalId;
	public String deviceCode;
	public String purpose;
	public ErrorInfo error;
	public String [] specVersion;
	public String [] deviceSubId;
}
