package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

import io.mosip.proxy.abis.constant.FailureReasonsConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InsertRequestMO represents the data transfer object (DTO) for an insert
 * request to the ABIS (Automated Biometric Identification System).
 * <p>
 * This class encapsulates fields such as ID, version, request ID, request time,
 * reference ID, and reference URL, ensuring validation of necessary attributes
 * using annotations from the Jakarta Bean Validation API.
 * </p>
 * <p>
 * Instances of this class are used to carry request information from clients to
 * the ABIS proxy for processing.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsertRequestMO {
	/**
	 * The ID of the insert request.
	 */
	@Pattern(regexp = "mosip.abis.insert", message = FailureReasonsConstants.INVALID_ID)
	private String id;

	/**
	 * The version number of the insert request.
	 */
	@SuppressWarnings({ "java:S6353" })
	@Pattern(regexp = "[0-9]+.[0-9]", message = FailureReasonsConstants.INVALID_VERSION)
	private String version;

	/**
	 * The unique request ID associated with the insert request.
	 */
	@NotEmpty(message = FailureReasonsConstants.MISSING_REQUESTID)
	@NotNull(message = FailureReasonsConstants.MISSING_REQUESTID)
	private String requestId;

	/**
	 * The timestamp when the insert request was made.
	 */
	@Column(name = "requesttime")
	@NotNull(message = FailureReasonsConstants.MISSING_REQUESTTIME)
	private LocalDateTime requesttime;

	/**
	 * The unique reference ID linked to the data being inserted.
	 */
	@Id
	@NotEmpty(message = FailureReasonsConstants.MISSING_REFERENCEID)
	@NotNull(message = FailureReasonsConstants.MISSING_REFERENCEID)
	private String referenceId;

	/**
	 * The URL reference associated with the data being inserted.
	 */
	@NotEmpty(message = FailureReasonsConstants.MISSING_REFERENCE_URL)
	@NotNull(message = FailureReasonsConstants.MISSING_REFERENCE_URL)
	@Column(name = "referenceURL")
	private String referenceURL;
}