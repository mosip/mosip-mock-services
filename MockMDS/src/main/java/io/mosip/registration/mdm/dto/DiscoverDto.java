package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DiscoverDto {
	private String deviceId;
	private String deviceStatus;
	private String certification;
	private String serviceVersion;
	private String callbackId;
	private String digitalId;
	private String deviceCode;
	private String purpose;
	private ErrorInfo error;
	private String [] specVersion;
	private String [] deviceSubId;
}