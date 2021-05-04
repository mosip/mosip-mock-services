package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.entity.Expectation;

public interface ExpectationCache {
	
	public void delete(String id);

	public void insert(Expectation ie);

	public Expectation get(String id);
	
}
