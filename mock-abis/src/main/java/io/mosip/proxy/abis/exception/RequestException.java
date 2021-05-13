package io.mosip.proxy.abis.exception;

import io.mosip.proxy.abis.entity.RequestMO;

public class RequestException extends RuntimeException {

	public RequestMO entity = null;
	
	public String reasonConstant=null;

	public int delayResponse=0;

	public RequestException() {
		super();

	}

	public RequestException(RequestMO ie,String reasonConstant) {
		super();
		this.entity = ie;
		this.reasonConstant=reasonConstant;
	}

	public RequestException(String reasonConstant) {
		super();
		this.reasonConstant=reasonConstant;
	}

	public RequestException(String reasonConstant, int d) {
		super();
		this.reasonConstant=reasonConstant;
		this.delayResponse=d;
	}

	public RequestMO getEntity() {
		return entity;
	}

	public void setEntity(RequestMO entity) {
		this.entity = entity;
	}

	public String getReasonConstant() {
		return reasonConstant;
	}

	public void setReasonConstant(String reasonConstant) {
		this.reasonConstant = reasonConstant;
	}

	public int getDelayResponse() {
		return delayResponse;
	}

	public void setDelayResponse(int d) {
		this.delayResponse = d;
	}
	
	

}
