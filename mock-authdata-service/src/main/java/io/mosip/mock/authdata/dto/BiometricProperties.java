package io.mosip.mock.authdata.dto;

import lombok.Data;

@Data
public class BiometricProperties {

	private String format;
	private double version;
	private String value;
}
