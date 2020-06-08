package io.mosip.mds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceDto {
	
	@ApiModelProperty(value = "port", required = true, dataType = "java.lang.String")
	public String port;
	
	@ApiModelProperty(value = "discoverInfo", required = true, dataType = "java.lang.String")
	public String discoverInfo;

}
