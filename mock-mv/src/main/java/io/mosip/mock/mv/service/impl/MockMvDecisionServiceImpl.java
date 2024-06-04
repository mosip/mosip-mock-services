package io.mosip.mock.mv.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;
import io.mosip.mock.mv.service.MockMvDecisionService;

@Service
public class MockMvDecisionServiceImpl implements MockMvDecisionService {
	@Value("${mock.mv.default.decision}")
	private String mockMvDecision;

	private ExpectationCache expectationCache;

	@Autowired
	public MockMvDecisionServiceImpl(ExpectationCache expectationCache) {
		this.expectationCache = expectationCache;
	}
	
	@Override
	public String getMockMvDecision() {
		return mockMvDecision;
	}

	@Override
	public void setMockMvDecision(String mockDecision) {
		mockMvDecision = mockDecision;
	}

	@Override
	public Map<String, Expectation> getExpectations() {
		return expectationCache.get();
	}

	@Override
	public void setExpectation(Expectation exp) {
		expectationCache.insert(exp);
	}

	@Override
	public void deleteExpectation(String id) {
		expectationCache.delete(id);
	}

	@Override
	public void deleteExpectations() {
		expectationCache.deleteAll();
	}

	@Override
	public Expectation getExpectation(String rid) {
		return expectationCache.get(rid);
	}
}