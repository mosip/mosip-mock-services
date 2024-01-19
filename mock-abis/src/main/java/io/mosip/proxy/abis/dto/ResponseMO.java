package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;

public class ResponseMO {
	private String id;
	private String requestId;
	private LocalDateTime responsetime;
	private String returnValue;

	public ResponseMO(String id, String requestId, LocalDateTime responsetime, String returnValue) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.responsetime = responsetime;
		this.returnValue = returnValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public LocalDateTime getResponsetime() {
		return responsetime;
	}

	public void setResponsetime(LocalDateTime responsetime) {
		this.responsetime = responsetime;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
}