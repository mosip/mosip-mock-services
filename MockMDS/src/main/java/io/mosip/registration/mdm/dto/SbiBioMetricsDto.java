package io.mosip.registration.mdm.dto;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SbiBioMetricsDto {

	private String specVersion;
	private String data;
	private String hash;
	
	private HashMap<String, String> additionalInfo;
	
	private Error error;
}
