package io.mosip.mds.dto;

import java.util.List;

import io.mosip.mds.dto.getresponse.UIInput;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ComposeRequestDto {

	@ApiModelProperty(value = "runId", required = true, dataType = "java.lang.String")
	private String runId;

	@ApiModelProperty(value = "testId", required = true, dataType = "java.lang.String")
	private String testId;

	private List<UIInput> uiInputs;

	@ApiModelProperty(value = "deviceInfo", required = true, dataType = "java.lang.String")
	private String deviceInfo;
}
