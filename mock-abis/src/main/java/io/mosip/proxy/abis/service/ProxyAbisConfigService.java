package io.mosip.proxy.abis.service;

import io.mosip.proxy.abis.dto.Expectation;

import java.util.List;
import java.util.Map;

public interface ProxyAbisConfigService {
	public Boolean getDuplicate();
	public void setDuplicate(Boolean d);
	public Boolean isForceDuplicate();
	public Map<String, Expectation> getExpectations();
	public void setExpectation(Expectation exp);
	public void deleteExpectation(String id);
	public void deleteExpectations();
	public List<String> getCachedBiometrics();
	public List<String> getCachedBiometric(String hash);
	public void deleteAllCachedBiometrics();
}
