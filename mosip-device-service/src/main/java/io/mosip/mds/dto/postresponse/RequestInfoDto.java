package io.mosip.mds.dto.postresponse;

import java.util.HashMap;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RequestInfoDto {
	
	@ApiModelProperty(value = "url", required = true, dataType = "java.lang.String")
	public String url;
	
	@ApiModelProperty(value = "verb", required = true, dataType = "java.lang.String")
	public String verb;
	
	public HashMap<String, String> headers;
	
	@ApiModelProperty(value = "body", required = true, dataType = "java.lang.String")
	public String body;
	

}
