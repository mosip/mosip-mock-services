package io.mosip.mds.dto.postresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ValidationResult {

	@ApiModelProperty(value = "validationName", required = true, dataType = "java.lang.String")
	public String validationName;

	@ApiModelProperty(value = "validationDescription", required = true, dataType = "java.lang.String")
	public String validationDescription;
	
	@ApiModelProperty(value = "status", required = true, dataType = "java.lang.String")
	public String status;
	
	@ApiModelProperty(value = "errors", required = true, dataType = "java.lang.String")
	public List<String> errors = new ArrayList<String>();
}
