package io.mosip.mock.authentication.dto;

import lombok.Data;

@Data
public class BiometricProperties {

	private String format;
	private double version;
	private String value;
}
