package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BioMetricsDataDto {
	private String digitalId;
	private String deviceCode;
	private String deviceServiceVersion;
	private String bioType;
	private String bioSubType;
	private String purpose;
	private String env;
	private String domainUri;
	private String bioValue;
	private String bioExtract;
	private String registrationId;
	private String transactionId;
	private String timestamp;
	private String requestedScore;
	private String qualityScore;
}
