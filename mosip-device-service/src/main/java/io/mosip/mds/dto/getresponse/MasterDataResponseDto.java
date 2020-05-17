package io.mosip.mds.dto.getresponse;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MasterDataResponseDto {
	
	
	public List<BiometricTypeDto> biometricType = new ArrayList<>();
	
	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.util.List<java.lang.String>")
	public List<String> mdsSpecificationVersion = new ArrayList<>();
	
	public List<String> process = new ArrayList<>();

}
