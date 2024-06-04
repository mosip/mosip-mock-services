package io.mosip.registration.mdm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties
public class DeviceInfoDto {
	private String deviceInfo;
	private ErrorInfo error;
}