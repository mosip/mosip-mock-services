package io.mosip.proxy.abis.exception;

import java.io.Serializable;

import io.mosip.proxy.abis.dto.RequestMO;

public class RequestException extends RuntimeException implements Serializable {
	private final RequestMO entity;
	private final String reasonConstant;
	private final int delayResponse;

	public RequestException() {
		super();
		this.entity = new RequestMO();
		this.reasonConstant = null;
		this.delayResponse = 0;
	}

	public RequestException(RequestMO ie, String reasonConstant, int delayResponse) {
		super();
		this.entity = ie;
		this.reasonConstant = reasonConstant;
		this.delayResponse = delayResponse;
	}

	public RequestException(RequestMO ie, String reasonConstant) {
		this(ie, reasonConstant, 0);
	}

	public RequestException(String reasonConstant) {
		this(new RequestMO(), reasonConstant, 0);
	}

	public RequestException(String reasonConstant, int delayResponse) {
		this(new RequestMO(), reasonConstant, delayResponse);
	}

	public RequestMO getEntity() {
		return entity;
	}

	public String getReasonConstant() {
		return reasonConstant;
	}

	public int getDelayResponse() {
		return delayResponse;
	}
}