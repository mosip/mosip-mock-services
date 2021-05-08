package io.mosip.proxy.abis.exception;

import io.mosip.proxy.abis.entity.RequestMO;

public class RequestException extends RuntimeException {

	public RequestMO entity = null;
	
	public String reasonConstant=null;

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
	
	

}
