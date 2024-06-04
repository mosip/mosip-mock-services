package io.mosip.registration.mdm.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RCaptureResponse {
	private List<BioMetricsDto> biometrics;
}