package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidateResponseRequestDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	public String runId;

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;

	@ApiModelProperty(value = "mdsResponse", required = true, dataType = "java.lang.String")
	public String mdsResponse;

	@ApiModelProperty(value = "mdsRequest", required = true, dataType = "java.lang.String")
	public String mdsRequest;

	@ApiModelProperty(value = "resultVerbosity", required = true, dataType = "java.lang.String")
	public String resultVerbosity;
	
}
