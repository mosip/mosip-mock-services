package io.mosip.mock.mv.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Represents a wrapper for API responses, containing metadata and optional
 * errors.
 * <p>
 * This class encapsulates the response data along with identification,
 * versioning, and timestamp information. It can also hold a list of errors
 * encountered during the processing of the request.
 *
 * @param <T> the type of the response object wrapped by this class
 */
@Data
public class ResponseWrapper<T> {

	/**
	 * Unique identifier for the response.
	 */
	private String id;

	/**
	 * Version of the response format.
	 */
	private String version;

	/**
	 * Timestamp indicating when the response was generated.
	 */
	private String responsetime;

	/**
	 * The actual response object encapsulated by this wrapper.
	 */
	private T response;

	/**
	 * List of errors encountered during processing, if any.
	 */
	private List<ErrorDTO> errors = new ArrayList<>();
}