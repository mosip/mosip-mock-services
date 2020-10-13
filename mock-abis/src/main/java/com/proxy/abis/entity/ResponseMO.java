package com.proxy.abis.entity;

import java.time.LocalDateTime;




public class ResponseMO {
	
	private String id;
	
	private String requestId;
	
	private LocalDateTime responseTime;
	
	private int returnValue;
	
	

	public ResponseMO(String id, String requestId, LocalDateTime responseTime, int returnValue) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.responseTime = responseTime;
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

	public LocalDateTime getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(LocalDateTime responseTime) {
		this.responseTime = responseTime;
	}

	public int getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(int returnValue) {
		this.returnValue = returnValue;
	}
	

}
