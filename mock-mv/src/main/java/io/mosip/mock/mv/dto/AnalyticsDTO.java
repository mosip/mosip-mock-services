package io.mosip.mock.mv.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing analytics related to verification
 * operations.
 * <p>
 * This class encapsulates various analytics data such as operator IDs,
 * comments, and additional custom analytics provided as key-value pairs.
 * <p>
 * It provides a structured format to manage and retrieve analytics information
 * within the context of verification processes.
 * 
 */
@Data
public class AnalyticsDTO {
	/** The primary operator ID involved in verification. */
	private String primaryOperatorID;

	/** Comments provided by the primary operator during verification. */
	private String primaryOperatorComments;

	/** The secondary operator ID involved in verification, if applicable. */
	private String secondaryOperatorID;

	/**
	 * Comments provided by the secondary operator during verification, if
	 * applicable.
	 */
	private String secondaryOperatorComments;

	/** Additional analytics stored as key-value pairs for flexibility. */
	private Map<String, String> analytics = new HashMap<>();
}