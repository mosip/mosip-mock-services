package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaptureRequestDto{

	public String env;
	public String purpose;
	public String specVersion;
	public int timeout;
	@JsonIgnore
	public String domainUri;
	@JsonIgnore
	public String captureTime;
	public String transactionId;

	@JsonProperty("bio")
	public List<CaptureRequestDeviceDetailDto> bio;

	@JsonIgnore
	public List<Map<String, String>> customOpts;
}
