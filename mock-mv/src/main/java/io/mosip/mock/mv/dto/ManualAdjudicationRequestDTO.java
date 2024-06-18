package io.mosip.mock.mv.dto;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a manual adjudication request.
 * <p>
 * This class encapsulates various attributes related to a manual adjudication
 * request, including an identifier, version, request ID, reference ID, request
 * time, reference URL, additional information, and a gallery.
 */
@Data
public class ManualAdjudicationRequestDTO {

	/**
	 * The unique identifier of the adjudication request.
	 */
	private String id;

	/**
	 * The version of the adjudication request.
	 */
	private String version;

	/**
	 * The identifier of the request.
	 */
	private String requestId;

	/**
	 * The reference identifier associated with the request.
	 */
	private String referenceId;

	/**
	 * The timestamp indicating when the request was made.
	 */
	private String requesttime;

	/**
	 * The URL pointing to the reference related to the request.
	 */
	private String referenceURL;

	/**
	 * Additional information related to the adjudication request.
	 */
	private List<Addtional> addtional;

	/**
	 * The gallery associated with the adjudication request.
	 */
	private Gallery gallery;
}