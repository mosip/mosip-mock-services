package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class NewBioAuthDto {
	private String digitalId;
	private String deviceCode;
	private String deviceServiceVersion;
	private String bioSubType;
	private String purpose;
	private String env;
	private String bioType;
	private String bioValue;
	private String transactionId;
	private String timestamp;
	private String requestedScore;
	private String qualityScore;
	private String domainUri;
}