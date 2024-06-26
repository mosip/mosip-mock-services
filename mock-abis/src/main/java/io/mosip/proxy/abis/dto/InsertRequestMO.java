package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.mosip.proxy.abis.exception.FailureReasonsConstants;

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
public class InsertRequestMO {
	@Pattern(regexp = "mosip.abis.insert", message = FailureReasonsConstants.INVALID_ID)
	private String id;

	@Pattern(regexp = "[0-9]+.[0-9]", message = FailureReasonsConstants.INVALID_VERSION)
	private String version;

	@NotEmpty(message = FailureReasonsConstants.MISSING_REQUESTID)
	@NotNull(message = FailureReasonsConstants.MISSING_REQUESTID)
	private String requestId;

	@Column(name = "requesttime")
	@NotNull(message = FailureReasonsConstants.MISSING_REQUESTTIME)
	private LocalDateTime requesttime;

	@Id
	@NotEmpty(message = FailureReasonsConstants.MISSING_REFERENCEID)
	@NotNull(message = FailureReasonsConstants.MISSING_REFERENCEID)
	private String referenceId;

	@NotEmpty(message = FailureReasonsConstants.MISSING_REFERENCE_URL)
	@NotNull(message = FailureReasonsConstants.MISSING_REFERENCE_URL)
	@Column(name = "referenceURL")
	private String referenceURL;

	public InsertRequestMO() {
		super();
	}

	public InsertRequestMO(String id, String version, String requestId, LocalDateTime requesttime, String referenceId,
			String referenceURL) {
		super();
		this.id = id;
		this.version = version;
		this.requestId = requestId;
		this.requesttime = requesttime;
		this.referenceId = referenceId;
		this.referenceURL = referenceURL;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public LocalDateTime getRequesttime() {
		return requesttime;
	}

	public void setRequesttime(LocalDateTime requesttime) {
		this.requesttime = requesttime;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getReferenceURL() {
		return referenceURL;
	}

	public void setReferenceURL(String referenceURL) {
		this.referenceURL = referenceURL;
	}
}