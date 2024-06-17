package io.mosip.proxy.abis.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.mosip.proxy.abis.dto.IdentityRequest.Flags;
import io.mosip.proxy.abis.dto.IdentityRequest.Gallery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RequestMO represents a request message object.
 * <p>
 * This class encapsulates information about a request message, including an ID,
 * version, request ID, request timestamp, and reference ID.
 * </p>
 * <p>
 * Instances of this class are typically used to construct and send request
 * messages to a service or API.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMO implements Serializable {
	/**
	 * Unique identifier for the request message.
	 */
	private String id;

	/**
	 * Version of the request message format or protocol.
	 */
	private String version;

	/**
	 * Identifier for the request message.
	 */
	private String requestId;

	/**
	 * Timestamp indicating when the request was created.
	 */
	private LocalDateTime requesttime;

	/**
	 * Identifier referencing another entity or operation.
	 */
	private String referenceId;
}