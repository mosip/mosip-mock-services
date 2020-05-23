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

	private String env;
	private String purpose;
	private String specVersion;
	private int timeout;
	@JsonIgnore
	private String captureTime;
	@JsonIgnore
	private String registrationID;

	@JsonProperty("bio")
	private List<CaptureRequestDeviceDetailDto> mosipBioRequest;

	private List<Map<String, String>> customOpts;
}
