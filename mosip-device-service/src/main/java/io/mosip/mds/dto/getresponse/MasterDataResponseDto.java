package io.mosip.mds.dto.getresponse;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MasterDataResponseDto {
	
	
	private List<BiometricTypeDto> biometricType;
	
	@ApiModelProperty(value = "mdsSpecificationVersion", required = true, dataType = "java.lang.String")
	private String mdsSpecificationVersion;
	
	private List<String> process;

}
