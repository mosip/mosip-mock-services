package io.mosip.mds.dto.postresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ComposeRequestResponseDto {
	
	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	public String runId;
	
	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;
	

	public RequestInfoDto requestInfoDto;

}
