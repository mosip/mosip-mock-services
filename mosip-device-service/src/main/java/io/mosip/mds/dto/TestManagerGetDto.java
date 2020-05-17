package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestManagerGetDto {

	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.lang.String")
	public String mdsSpecificationVersion;
	
	@ApiModelProperty(value = "process", required = true, dataType = "java.lang.String")
	public String process;
	
	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	public String biometricType;
	
	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.lang.String")
	public String deviceType;
}
