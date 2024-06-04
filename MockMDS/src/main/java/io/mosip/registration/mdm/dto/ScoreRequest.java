package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ScoreRequest {
	private String type;
	private String qualityScore;
	private boolean fromIso;
}