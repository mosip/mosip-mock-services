package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamingRequestDetail{

	public String deviceId;
	public String deviceSubId;
	public int timeout;
}