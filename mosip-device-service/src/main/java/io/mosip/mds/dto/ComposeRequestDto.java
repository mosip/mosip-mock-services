package io.mosip.mds.dto;

import java.util.List;

import io.mosip.mds.dto.getresponse.UIInput;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ComposeRequestDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	public String runId;

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	public String testId;

	public List<UIInput> uiInputs;
	
	public DeviceDto deviceInfo;
}
