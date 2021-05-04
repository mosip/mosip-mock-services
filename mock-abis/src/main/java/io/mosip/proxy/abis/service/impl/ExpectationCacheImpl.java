package io.mosip.proxy.abis.service;

import java.util.Map;

import io.mosip.proxy.abis.entity.Expectation;

@Component
public class ExpectationCacheImpl implements ExpectationCache {
	
	Map<String, Expectation> expectationMap = new ConcurrentHashMap<String, Expectation>();

	public boolean delete(String id){
		return expectationMap.remove(id)==null?false:true;
	}

	public void insert(Expectation expectation){
		expectationMap.put(expectation.getId(), expectation);
	}

	public Expectation get(String id) {
		return expectationMap.getOrDefault(id, new Expectation());
	}
}
