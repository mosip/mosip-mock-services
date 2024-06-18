package io.mosip.mock.mv.dto;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a gallery of reference IDs.
 * <p>
 * This class encapsulates a list of reference IDs associated with a gallery.
 */
@Data
public class Gallery {

	/**
	 * The list of reference IDs contained within the gallery.
	 */
	private List<ReferenceIds> referenceIds;
}