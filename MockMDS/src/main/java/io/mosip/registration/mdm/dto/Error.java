package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties(value = {"errorCode", "errorInfo"}, ignoreUnknown = true)
public class Error {
	public String errorCode;
    public String errorInfo;
    
    public Error() {
		
	}
	public Error(String errorCode, String errorInfo) {
		this.errorCode = errorCode;
		this.errorInfo = errorInfo;
	}  
}
