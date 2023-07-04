package io.mosip.mock.mv.dto;

public class Expectation {

    private String rid;
    
    private String mockMvDecision;

    private int delayResponse = 0;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public int getDelayResponse() {
        return delayResponse;
    }

    public void setDelayResponse(int delayResponse) {
        this.delayResponse = delayResponse;
    }

    public String getRId() {
        return rid;
    }

    public void setRId(String rid) {
        this.rid = rid;
    }

    public Expectation(String rid, String mockMvDecision, int delayResponse) {
        this.rid = rid;
        this.mockMvDecision = mockMvDecision;
        this.delayResponse = delayResponse;
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

    @Override
    public String toString() {
        return "Expectation{" +
                "rid='" + rid + '\'' +
                ", mockMvDecision='" + mockMvDecision + '\'' +
                ", delayResponse=" + delayResponse +
                '}';
    }
}