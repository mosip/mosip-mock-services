package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Represents a reference identifier and its corresponding URL.
 * <p>
 * This class encapsulates a pair of values: a reference identifier and a URL
 * pointing to additional information related to the identifier.
 */
@Data
public class ReferenceIds {

	/**
	 * The unique reference identifier.
	 */
	private String referenceId;

	/**
	 * The URL associated with the reference identifier.
	 */
	private String referenceURL;
}