package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DelayRequest {
	public String type;
	public String delay;
	public String []method;
}
