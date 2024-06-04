package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StreamingRequestDetail {
	private String deviceId;
	private String deviceSubId;
	private String timeout;
}