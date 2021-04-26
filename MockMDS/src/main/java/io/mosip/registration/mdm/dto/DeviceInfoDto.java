package io.mosip.registration.mdm.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceInfoDto {
	public String deviceInfo;
	public Error error;
}
