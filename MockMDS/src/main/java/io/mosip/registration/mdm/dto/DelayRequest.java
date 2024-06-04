package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DelayRequest {
	private String type;
	private String delay;
	private String[] method;
}