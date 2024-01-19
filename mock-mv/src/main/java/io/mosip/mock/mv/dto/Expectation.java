package io.mosip.mock.mv.dto;

public class Expectation {

    private String rid;
    
    private String mockMvDecision;

    private int delayResponse;

    public String getRId() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public void setmockMvDecision(String mockMvDecision)
    {
        this.mockMvDecision=mockMvDecision;
    }


    public int getDelayResponse() {
        return delayResponse;
    }

    public void setDelayResponse(int delayResponse) {
        this.delayResponse = delayResponse;
    }



    public Expectation(String rid,String mockMvDecision,int delayResponse) {
        super();
        this.rid = rid;
        this.mockMvDecision=mockMvDecision;
        this.delayResponse=delayResponse;
    } 

	public Expectation() {
		super();
	}

	public String getMockMvDecision() {
		return mockMvDecision;
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