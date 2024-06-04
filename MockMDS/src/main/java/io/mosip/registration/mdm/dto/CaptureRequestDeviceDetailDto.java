package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CaptureRequestDeviceDetailDto {
	private String type;
	private String count;
	private String[] exception;
	private String[] bioSubType;
	private int requestedScore;
	private String deviceId;
	private String deviceSubId;
	private String previousHash;
}