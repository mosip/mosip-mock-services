package io.mosip.registration.mdm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ProfileRequest {
	private String type;
	private String profileId;
}