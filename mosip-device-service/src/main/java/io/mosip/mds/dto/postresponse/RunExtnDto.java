package io.mosip.mds.dto.postresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RunExtnDto {
	
	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	public String runId;

	@ApiModelProperty(value = "tests", required = true, dataType = "java.lang.List<String>")
	public String[] tests;

	@ApiModelProperty(value = "runStatus", required = true, dataType = "java.lang.String")
	public String runStatus;


}
