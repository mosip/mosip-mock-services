package io.mosip.mock.mv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MockMvDecisionService {
	
	@Value("${mock.mv.decision}")
	private String mockMvDecision;

	public String getMockMvDecision() {
		return mockMvDecision;
	}

	public void setMockMvDecision(String mockMvDecision) {
		this.mockMvDecision = mockMvDecision;
	}

}
