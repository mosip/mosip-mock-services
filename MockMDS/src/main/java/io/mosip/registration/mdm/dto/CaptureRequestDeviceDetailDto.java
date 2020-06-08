package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaptureRequestDeviceDetailDto {

	public String type;
	public String[] bioSubType;
	public int count;
	public String[] exception;
	public String[] bioSubType;
	public int requestedScore;
	public String deviceId;
	public String deviceSubId;
	public String previousHash;

}
