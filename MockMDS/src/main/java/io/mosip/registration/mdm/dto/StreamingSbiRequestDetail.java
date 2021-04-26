package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamingSbiRequestDetail {

	private String serialNo;
	private String deviceSubId;
	private String timeout;
	private String dimensions;
}
