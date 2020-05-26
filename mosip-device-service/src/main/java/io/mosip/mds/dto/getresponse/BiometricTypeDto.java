package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BiometricTypeDto {
	
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	public String type;
	
	@ApiModelProperty(value = "deviceType", required = true, dataType = "java.util.List")
	public List<String> deviceType = new ArrayList<>();
	
	
	public List<String> segments = new ArrayList<>();
	public BiometricTypeDto(String bioType)
	{
		type = bioType;
	}

	protected BiometricTypeDto()
	{

	}

}
