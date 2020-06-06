package io.mosip.registration.mdm.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DiscoverDto {

	private String deviceId;
	private String deviceStatus;
	private String certification;
	private String serviceVersion;
	private String callbackId;
	private String digitalId;
	private String deviceCode;
	private String purpose;
	private Map<String, String> error;
	private String [] specVersion;
	private String [] deviceSubId;

}
