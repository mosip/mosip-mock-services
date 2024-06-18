package io.mosip.proxy.abis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IdentifyDelayResponse encapsulates the delay response configuration and the result of an identity operation.
 * <p>
 * This class combines an integer value representing the delay response time in milliseconds and an IdentityResponse object,
 * which contains the details of the identity operation outcome.
 * </p>
 * <p>
 * Use this class to convey both the configured delay response and the identity response data, facilitating handling and communication
 * of delayed identity operation results in ABIS (Automated Biometric Identification System) interactions.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifyDelayResponse {
	private IdentityResponse identityResponse;
	private int delayResponse = 0;
}