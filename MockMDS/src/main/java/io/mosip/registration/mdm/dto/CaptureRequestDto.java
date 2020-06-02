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
	public String captureTime;
	@JsonIgnore
	public String registrationID;

	@JsonProperty("bio")
	public List<CaptureRequestDeviceDetailDto> mosipBioRequest;

	public List<Map<String, String>> customOpts;
}
