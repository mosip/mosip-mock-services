package io.mosip.mock.mv.service;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;

public interface MockMvDecisionService {

	public String getMockMvDecision();

	public void setMockMvDecision(String mockMvDecision);

	public Map<String, Expectation> getExpectations();

	public Expectation getExpectation(String rid);

	public void setExpectation(Expectation exp);

	public void deleteExpectation(String rid);

	public void deleteExpectations();
}