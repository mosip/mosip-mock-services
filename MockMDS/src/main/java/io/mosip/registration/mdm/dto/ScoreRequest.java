package io.mosip.registration.mdm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ScoreRequest {
	public String type;
	public String qualityScore;
	public boolean fromIso;
}
