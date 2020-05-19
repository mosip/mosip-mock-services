package io.mosip.mds.dto.getresponse;

import java.util.List;

import io.mosip.mds.entitiy.Validator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestExtnDto {

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;
	
	@ApiModelProperty(value = "testDescription", required = true, dataType = "java.lang.String")
	public String testDescription;
	
	@ApiModelProperty(value = "requestGenerator", required = true, dataType = "java.lang.String")
	public String requestGenerator;
		
	public List<UIInput> uiInput;

	public List<String> processes;

	public List<String> biometricTypes;

	public List<String> deviceTypes;

	public List<String> mdsSpecVersions;

	public List<Validator> validators;



}
