package io.mosip.mock.mv.dto;

import io.swagger.models.auth.In;

public class Expectation {

    private String rid;
    
    private String mockMvDecision;
    private Integer delayInResponse;

    public Integer getDelayInResponse() {
        return delayInResponse;
    }

    public void setDelayInResponse(Integer delayInResponse) {
        this.delayInResponse = delayInResponse;
    }

    public String getRId() {
        return rid;
    }

    public void setRId(String rid) {
        this.rid = rid;
    }

    public Expectation(String rid, int delayInResponse, String mockMvDecision) {
        super();
        this.rid = rid;
        this.mockMvDecision=mockMvDecision;
        this.delayInResponse=delayInResponse;
    } 

	public Expectation() {
		super();
	}

	public String getMockMvDecision() {
		return mockMvDecision;
	}

	public void setMockMvDecision(String mockMvDecision) {
		this.mockMvDecision = mockMvDecision;
	}

}