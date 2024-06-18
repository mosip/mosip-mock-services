package io.mosip.proxy.abis.exception;

import java.io.Serializable;

import io.mosip.proxy.abis.dto.RequestMO;
import lombok.Getter;

/**
 * Exception thrown to indicate a failure in processing an insert request.
 * <p>
 * This exception encapsulates information about the failed request, including
 * the {@code entity} associated with the request, {@code reasonConstant}
 * indicating the reason for failure, and optional {@code delayResponse}
 * specifying delay in response, if any.
 * </p>
 * <p>
 * Use this exception to handle and propagate errors related to insert requests
 * in the ABIS (Automated Biometric Identification System) or similar systems
 * requiring structured exception handling for request processing failures.
 * </p>
 * 
 * @since 1.0.0
 */
@Getter
public class RequestException extends RuntimeException implements Serializable {
	/** Serial version UID for serialization. */
	private static final long serialVersionUID = -428803217785639428L;

	/** The entity associated with the failed request. */
	private final RequestMO entity;

	/** The reason constant indicating the cause of failure. */
	private final String reasonConstant;

	/** Optional delay in response due to processing. */
	private final int delayResponse;

	/**
	 * Constructs a new {@code RequestException} with default values. Sets
	 * {@code entity} to a new {@code RequestMO}, {@code reasonConstant} to
	 * {@code null}, and {@code delayResponse} to {@code 0}.
	 */
	public RequestException() {
		super();
		this.entity = new RequestMO();
		this.reasonConstant = null;
		this.delayResponse = 0;
	}

	/**
	 * Constructs a new {@code RequestException} with the specified parameters.
	 * 
	 * @param entity         The entity associated with the failed request.
	 * @param reasonConstant The reason constant indicating the cause of failure.
	 * @param delayResponse  Optional delay in response due to processing.
	 */
	public RequestException(RequestMO ie, String reasonConstant, int delayResponse) {
		super();
		this.entity = ie;
		this.reasonConstant = reasonConstant;
		this.delayResponse = delayResponse;
	}

	/**
	 * Constructs a new {@code RequestException} with the specified entity and
	 * reason constant. Sets {@code delayResponse} to {@code 0}.
	 * 
	 * @param entity         The entity associated with the failed request.
	 * @param reasonConstant The reason constant indicating the cause of failure.
	 */
	public RequestException(RequestMO ie, String reasonConstant) {
		this(ie, reasonConstant, 0);
	}

	/**
	 * Constructs a new {@code RequestException} with the specified reason constant.
	 * Sets {@code entity} to a new {@code RequestMO} and {@code delayResponse} to
	 * {@code 0}.
	 * 
	 * @param reasonConstant The reason constant indicating the cause of failure.
	 */
	public RequestException(String reasonConstant) {
		this(new RequestMO(), reasonConstant, 0);
	}

	/**
	 * Constructs a new {@code RequestException} with the specified reason constant
	 * and delay in response. Sets {@code entity} to a new {@code RequestMO}.
	 * 
	 * @param reasonConstant The reason constant indicating the cause of failure.
	 * @param delayResponse  Optional delay in response due to processing.
	 */
	public RequestException(String reasonConstant, int delayResponse) {
		this(new RequestMO(), reasonConstant, delayResponse);
	}
}