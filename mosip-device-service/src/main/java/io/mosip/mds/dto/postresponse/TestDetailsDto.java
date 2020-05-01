package io.mosip.mds.dto.postresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestDetailsDto {

	@ApiModelProperty(value = "validationName", required = true, dataType = "java.lang.String")
	private String validationName;
	
	@ApiModelProperty(value = "status", required = true, dataType = "java.lang.String")
	private String status;
	
	@ApiModelProperty(value = "summary", required = true, dataType = "java.lang.String")
	private String summary;
}
