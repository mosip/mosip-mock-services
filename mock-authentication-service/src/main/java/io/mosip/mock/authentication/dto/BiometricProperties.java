package io.mosip.mock.authentication.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.mock.authentication.util.ErrorConstants;
import lombok.Data;

@Data
public class BiometricProperties {

	@NotBlank(message = ErrorConstants.INVALID_FORMAT)
	private String format;

	@NotBlank(message = ErrorConstants.INVALID_VERSION)
	private double version;

	@NotBlank(message = ErrorConstants.INVALID_VALUE)
	private String value;
}
