package io.mosip.proxy.abis.entity;

import java.time.LocalDateTime;

public class FailureResponse {
	
	private String id;
	
	private String requestId;
	
	private LocalDateTime responsetime;
	
	private int returnValue;
	
	private String failureReason;

	public FailureResponse()
	{
		super();
	}
		
	public FailureResponse(String id, String requestId, LocalDateTime responsetime, int returnValue,
			String failureReason) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.responsetime = responsetime;
		this.returnValue = returnValue;
		this.failureReason = failureReason;
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

	public int getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(int returnValue) {
		this.returnValue = returnValue;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	
	
}
