package io.mosip.mock.mv.dto;

public class Expectation {

    private String rid;
    
    private String mockMvDecision;


    public String getRId() {
        return rid;
    }

    public void setRId(String rid) {
        this.rid = rid;
    }

    public Expectation(String rid,String mockMvDecision) {
        super();
        this.rid = rid;
        this.mockMvDecision=mockMvDecision;
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