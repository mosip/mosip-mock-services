package io.mosip.registration.mdm.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonIgnoreProperties
public class DeviceSBIInfo {
	
	public DeviceSBISubType deviceInfo;
	public Error error;
}
