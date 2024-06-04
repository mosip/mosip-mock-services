package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties
@SuppressWarnings({ "java:S1700" })
public class ErrorInfo {
	private String errorCode;
	private String errorInfo;
    
	public ErrorInfo(String errorCode, String errorInfo) {
		super();
		this.errorCode = errorCode;
		this.errorInfo = errorInfo;
	}    
}
