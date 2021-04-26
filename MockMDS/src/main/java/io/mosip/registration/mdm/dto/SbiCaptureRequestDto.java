package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SbiCaptureRequestDto {
	
	private String env;
	private String purpose;
	private String specVersion;
	private String timeout;
	private String captureTime;
	private String transactionId;
	private String domainUri;
	
	@JsonProperty("bio")
	public List<CaptureRequestDeviceDetailSbiDto> bio;

	@JsonIgnore
	public List<Map<String, String>> customOpts;

}
