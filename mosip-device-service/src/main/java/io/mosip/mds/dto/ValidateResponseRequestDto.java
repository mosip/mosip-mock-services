package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidateResponseRequestDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	private String runId;

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	private String testId;

	@ApiModelProperty(value = "mdsResponse", required = true, dataType = "java.lang.String")
	private String mdsResponse;
	
	@ApiModelProperty(value = "resultVerbosity", required = true, dataType = "java.lang.String")
	private String resultVerbosity;
	
}
