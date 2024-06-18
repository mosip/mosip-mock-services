package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing additional information associated
 * with a request.
 * <p>
 * This class encapsulates additional details such as ABIS ID and corresponding
 * response, providing a structured format to handle and transmit supplementary
 * data alongside main requests.
 * <p>
 * It supports integration with various systems where additional information is
 * required to enrich the context of operations.
 * 
 */
@Data
public class Addtional {

	/**
	 * The ABIS (Automated Biometric Identification System) ID associated with the
	 * request.
	 */
	private String abisId;

	/**
	 * The response or outcome related to the ABIS ID within the context of the
	 * request.
	 */
	private String response;
}