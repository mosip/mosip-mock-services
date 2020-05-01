package io.mosip.mds.dto.postresponse;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidateResponseDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	private String runId;
	
	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	private String testId;
	
	@ApiModelProperty(value = "status", required = true, dataType = "java.lang.String")
	private String status;
	
	@ApiModelProperty(value = "summary", required = true, dataType = "java.lang.String")
	private String summary;
	
	@ApiModelProperty(value = "request", required = true, dataType = "java.lang.String")
	private String request;
	
	@ApiModelProperty(value = "response", required = true, dataType = "java.lang.String")
	private String response;
	

	private List<TestDetailsDto> details;
}
