package io.mosip.mds.dto.getresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UIInput {
	
	@ApiModelProperty(value = "field", required = true, dataType = "java.lang.String")
	private String field;
	
	@ApiModelProperty(value = "behaviour", required = true, dataType = "java.lang.String")
	private String behaviour;

}
