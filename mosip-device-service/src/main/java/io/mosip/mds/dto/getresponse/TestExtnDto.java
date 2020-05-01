package io.mosip.mds.dto.getresponse;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestExtnDto {

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	private String testId;
	
	@ApiModelProperty(value = "testDescription", required = true, dataType = "java.lang.String")
	private String testDescription;
	
	@ApiModelProperty(value = "requestGenerator", required = true, dataType = "java.lang.String")
	private String requestGenerator;
	
	@ApiModelProperty(value = "validator", required = true, dataType = "java.lang.String")
	private String validator;
	
	private List<UIInput> uiInput;
}
