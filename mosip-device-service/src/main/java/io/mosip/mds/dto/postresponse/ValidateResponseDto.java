package io.mosip.mds.dto.postresponse;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidateResponseDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	public String runId;
	
	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;
	
	@ApiModelProperty(value = "status", required = true, dataType = "java.lang.String")
	public String status;
	
	@ApiModelProperty(value = "summary", required = true, dataType = "java.lang.String")
	public String summary;
	
	@ApiModelProperty(value = "request", required = true, dataType = "java.lang.String")
	public String request;
	
	@ApiModelProperty(value = "response", required = true, dataType = "java.lang.String")
	public String response;
	

	public List<TestDetailsDto> details;
}
