package io.mosip.mds.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestManagerDto {

	public String email;

	@ApiModelProperty(value = "mdsSpecVersion", required = true, dataType = "java.lang.String")
	public String mdsSpecVersion;

	@ApiModelProperty(value = "process", required = true, dataType = "java.lang.String")
	public String process;

	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	public String biometricType;

	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.lang.String")
	public String deviceType;


	public List<String> tests;
	
}
