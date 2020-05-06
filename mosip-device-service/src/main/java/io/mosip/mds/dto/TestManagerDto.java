package io.mosip.mds.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestManagerDto {

	@ApiModelProperty(value = "mdsSpecVersion", required = true, dataType = "java.lang.String")
	private String mdsSpecVersion;

	@ApiModelProperty(value = "process", required = true, dataType = "java.lang.String")
	private String process;

	@ApiModelProperty(value = "biometricType", required = true, dataType = "java.lang.String")
	private String biometricType;

	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.lang.String")
	private String deviceType;


	private List<String> tests;
	
}
