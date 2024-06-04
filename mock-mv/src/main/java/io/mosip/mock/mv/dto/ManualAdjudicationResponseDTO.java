package io.mosip.mock.mv.dto;

import java.time.OffsetDateTime;

public class ManualAdjudicationResponseDTO {

	private String id;

	private String requestId;

	private String responsetime;

	private Integer returnValue;

	private CandidateList candidateList;

	public ManualAdjudicationResponseDTO() {
		super();
	}

	@SuppressWarnings({ "1172" })
	public ManualAdjudicationResponseDTO(String id, String requestId, String responsetime, Integer returnValue,
			CandidateList candidateList) {
		super();
		this.id = id;
		this.requestId = requestId;

		this.returnValue = returnValue;
		this.candidateList = candidateList;
		this.responsetime = OffsetDateTime.now().toInstant().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getResponsetime() {
		return responsetime;
	}

	public void setResponsetime(String responsetime) {
		this.responsetime = responsetime;
	}

	public Integer getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Integer returnValue) {
		this.returnValue = returnValue;
	}

	public CandidateList getCandidateList() {
		return candidateList;
	}

	public void setCandidateList(CandidateList candidateList) {
		this.candidateList = candidateList;
	}
}