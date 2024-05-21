package io.mosip.mock.mv.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;

@Component
public class ExpectationCacheImpl implements ExpectationCache {

	Map<String, Expectation> expectationMap = new ConcurrentHashMap<String, Expectation>();

	public boolean delete(String rid) {
		return expectationMap.remove(rid) != null;
	}

	public void deleteAll() {
		expectationMap = new ConcurrentHashMap<String, Expectation>();
	}

	public void insert(Expectation expectation) {
		expectationMap.put(expectation.getRId(), expectation);
	}

	public Map<String, Expectation> get() {
		return expectationMap;
	}

	@Override
	public Expectation get(String rid) {
		return expectationMap.getOrDefault(rid, new Expectation());

	}
}