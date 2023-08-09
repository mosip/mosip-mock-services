package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

public class RequestMO {
	private String id;
	private String version;
	private String requestId;
	private LocalDateTime requesttime;
	private String referenceId;

	public RequestMO() {
		super();
	}

	public RequestMO(String id, String version, String requestId, LocalDateTime requesttime, String referenceId) {
		super();
		this.id = id;
		this.version = version;
		this.requestId = requestId;
		this.requesttime = requesttime;
		this.referenceId = referenceId;
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
}