package io.mosip.mds.dto.getresponse;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BiometricTypeDto {
	
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	private String type;
	
	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.util.List")
	private List<String> deviceType;
	
	
	private List<String> segments;

}
