package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscoverSBIDto {

	public String serialNo;
	public String deviceStatus;
	public String certification;
	public String serviceVersion;
	public String callbackId;
	public String digitalId;
	public String purpose;
	public String [] specVersion;
	public String [] deviceSubId;
	
	public Error error;
}
