package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaptureRequestDeviceDetailSbiDto {

	public String type;
	//public String[] bioSubType;
	public int count;
	public String serialNo;
	public String[] bioSubType;
	public String[] exception;
	public int requestedScore;
	public String deviceSubId;
	public String previousHash;

}
