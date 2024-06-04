package io.mosip.mock.mv.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class AnalyticsDTO {
	private String primaryOperatorID;

	private String primaryOperatorComments;

	private String secondaryOperatorID;

	private String secondaryOperatorComments;

	private Map<String, String> analytics = new HashMap<>();
}