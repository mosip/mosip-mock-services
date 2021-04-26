package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SbiBioMetricsDataDto {
	
	private String digitalId;
	private String deviceCode;
	private String bioExtract;
	private String registrationId;
	private String bioType;
	private String deviceServiceVersion;
	private String bioSubType;
	private String purpose;
	private String env;
	private String bioValue;
	private String transactionId;
	private String timestamp;
	private String requestedScore;
	private String qualityScore;

}
