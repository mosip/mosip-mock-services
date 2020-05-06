package io.mosip.mds.dto.postresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RequestInfoDto {
	
	@ApiModelProperty(value = "url", required = true, dataType = "java.lang.String")
	private String url;
	
	@ApiModelProperty(value = "verb", required = true, dataType = "java.lang.String")
	private String verb;
	
	private HeaderDto headers;
	
	@ApiModelProperty(value = "body", required = true, dataType = "java.lang.String")
	private String body;
	

}
