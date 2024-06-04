package io.mosip.registration.mdm.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CaptureRequestDto{
	private String env;
	private String purpose;
	private String specVersion;
	private int timeout;
	@JsonIgnore
	private String domainUri;
	@JsonIgnore
	private String captureTime;
	private String transactionId;
	@JsonProperty("bio")
	private List<CaptureRequestDeviceDetailDto> bio;
	@JsonIgnore
	private List<Map<String, String>> customOpts;
}