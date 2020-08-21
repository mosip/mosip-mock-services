package io.mosip.registration.mdm.dto;

import lombok.Data;

/**
 * The Class CryptomanagerRequestDto.
 * 
 */
@Data
public class CryptomanagerRequestDto {
	String applicationId;
	String data;
	String referenceId;
	String salt;
	String timeStamp;
}