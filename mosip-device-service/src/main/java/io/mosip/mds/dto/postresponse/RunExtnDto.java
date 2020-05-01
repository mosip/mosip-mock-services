package io.mosip.mds.dto.postresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RunExtnDto {
	
	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	private String runId;

}
