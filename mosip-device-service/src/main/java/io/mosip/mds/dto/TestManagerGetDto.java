package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestManagerGetDto {

	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.lang.String")
	private String mdsSpecificationVersion;
	
	@ApiModelProperty(value = "process", required = true, dataType = "java.lang.String")
	private String process;
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	private String biometricType;
	
	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.lang.String")
	private String deviceType;
}
