package com.proxy.abis.exception;

import org.springframework.validation.BindingResult;

import com.proxy.abis.entity.InsertEntity;
import com.proxy.abis.entity.RequestMO;

public class BindingException extends Exception{


	public RequestMO  entity;
	public BindingResult bindingResult;
	
	private static final long serialVersionUID = 1L;
	
	public BindingException()
	{
		super();
	}
	
	public BindingException(RequestMO entity,BindingResult bindingResult)
	{
		super();
		this.bindingResult=bindingResult;
		this.entity=entity;
	}

	public RequestMO getEntity() {
		return entity;
	}

	public void setEntity(RequestMO entity) {
		this.entity = entity;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}

	public void setBindingResult(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
	}
	
	
	

}
