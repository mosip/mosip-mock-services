package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class BioMetricsDto {

	private String specVersion;
	private String data;
	private String hash;
	private String sessionKey;
	private String thumbprint;
	
	private Error error;
}
