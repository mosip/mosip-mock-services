package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class DeviceSBISubType {
	
	private String serialNo;
	public String[] specVersion;
	public String env;
    public String digitalId;
	public String purpose;
	public String serviceVersion;
	public String deviceStatus;
	public String firmware;
	public String certification;
	public int[] deviceSubId;
	public String callbackId;

}
