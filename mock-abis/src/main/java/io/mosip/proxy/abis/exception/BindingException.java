package io.mosip.proxy.abis.exception;

import java.io.Serializable;

import org.springframework.validation.BindingResult;

import io.mosip.proxy.abis.dto.RequestMO;

public class BindingException extends Exception implements Serializable {
	private static final long serialVersionUID = 4870396284624577010L;

	public final RequestMO entity;
	public final transient BindingResult bindingResult;

	public BindingException() {
		super();
		this.entity = new RequestMO();
		this.bindingResult = null;
	}

	public BindingException(RequestMO entity, BindingResult bindingResult) {
		super();
		this.bindingResult = bindingResult;
		this.entity = entity;
	}

	public RequestMO getEntity() {
		return entity;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}
}