package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaptureRequestDeviceDetailDto {

	private String type;
	private int count;
	private String[] exception;
	private int requestedScore;
	private String deviceId;
	private String deviceSubId;
	private String previousHash;

}
