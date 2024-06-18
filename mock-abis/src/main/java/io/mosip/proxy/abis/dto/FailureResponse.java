package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FailureResponse encapsulates the details of a failed operation response.
 * <p>
 * This class represents a response object used to convey information about
 * failed operations. It includes attributes such as the unique identifier
 * {@code id} and {@code requestId} associated with the failed request, the
 * {@code responsetime} indicating the time of the response, the {@code returnValue}
 * which may contain additional context or error codes, and {@code failureReason}
 * providing specific details or reasons for the failure.
 * </p>
 * <p>
 * Use this class to handle and communicate failed operation responses in ABIS
 * (Automated Biometric Identification System) interactions or any other system
 * requiring structured failure response information.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FailureResponse {
	private String id;
	private String requestId;
	private LocalDateTime responsetime;
	private String returnValue;
	private String failureReason;
}