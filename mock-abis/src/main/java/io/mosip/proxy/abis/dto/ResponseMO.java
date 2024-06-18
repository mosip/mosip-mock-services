package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

import io.mosip.proxy.abis.dto.IdentityRequest.Flags;
import io.mosip.proxy.abis.dto.IdentityRequest.Gallery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResponseMO represents a response message object.
 * <p>
 * This class encapsulates information about a response message, including an
 * ID, request ID, response timestamp, and return value.
 * </p>
 * <p>
 * Instances of this class are typically used to convey response data from a
 * service or API call.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMO {
	/**
	 * Unique identifier for the response message.
	 */
	private String id;

	/**
	 * Identifier for the corresponding request message.
	 */
	private String requestId;

	/**
	 * Timestamp indicating when the response was generated.
	 */
	private LocalDateTime responsetime;

	/**
	 * The actual value returned in the response message.
	 */
	private String returnValue;
}