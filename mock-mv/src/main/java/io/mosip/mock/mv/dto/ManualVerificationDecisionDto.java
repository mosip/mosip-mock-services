package io.mosip.mock.mv.dto;

import lombok.Data;

@Data
public class ManualVerificationDecisionDto {
	private String matchedRefType;
	private String mvUsrId;
	private String reasonCode;
	private String regId;
	private String statusCode;
}