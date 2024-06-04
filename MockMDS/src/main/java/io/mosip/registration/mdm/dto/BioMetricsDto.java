package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties
public class BioMetricsDto {
	private String specVersion;
	private String data;
	private String hash;
	private String sessionKey;
	private String thumbprint;	
	private ErrorInfo error;
	@JsonIgnore
	public List<Map<String, String>> additionalInfo;
}