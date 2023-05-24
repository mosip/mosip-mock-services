package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;
import java.util.List;

public class IdentityResponse {
	private String id;
	private String requestId;
	private LocalDateTime responsetime;
	private String returnValue;
	private CandidateList candidateList;
	private Analytics analytics = new Analytics();

	public IdentityResponse() {
		super();
	}

	public IdentityResponse(String id, String requestId, LocalDateTime responsetime, String returnValue,
			CandidateList candidateList, Analytics analytics) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.responsetime = responsetime;
		this.returnValue = returnValue;
		this.candidateList = candidateList;
		this.analytics = analytics;
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

	public LocalDateTime getResponsetime() {
		return responsetime;
	}

	public void setResponsetime(LocalDateTime responsetime) {
		this.responsetime = responsetime;
	}

	public CandidateList getCandidateList() {
		return candidateList;
	}

	public void setCandidateList(CandidateList candidateList) {
		this.candidateList = candidateList;
	}

	public Analytics getAnalytics() {
		return analytics;
	}

	public void setAnalytics(Analytics analytics) {
		this.analytics = analytics;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public static class CandidateList {
		private String count;
		private List<Candidates> candidates;

		public CandidateList() {
			super();
		}

		public CandidateList(String count, List<Candidates> candidates) {
			super();
			this.count = count;
			this.candidates = candidates;
		}

		public String getCount() {
			return count;
		}

		public void setCount(String count) {
			this.count = count;
		}

		public List<Candidates> getCandidates() {
			return candidates;
		}

		public void setCandidates(List<Candidates> candidates) {
			this.candidates = candidates;
		}
	}

	public static class Modalities {
		private String biometricType;
		private Analytics analytics;

		public Modalities() {
			super();
		}

		public Modalities(String biometricType, Analytics analytics) {
			super();
			this.biometricType = biometricType;
			this.analytics = analytics;
		}

		public String getBiometricType() {
			return biometricType;
		}

		public void setBiometricType(String biometricType) {
			this.biometricType = biometricType;
		}

		public Analytics getAnalytics() {
			return analytics;
		}

		public void setAnalytics(Analytics analytics) {
			this.analytics = analytics;
		}
	}

	public static class Analytics {
		private String confidence;
		private String internalScore;
		private String key1;
		private String key2;

		public Analytics() {
			super();
		}

		public Analytics(String confidence, String internalScore, String value1, String value2) {
			super();
			this.confidence = confidence;
			this.internalScore = internalScore;
			this.key1 = value1;
			this.key2 = value2;
		}

		public String getConfidence() {
			return confidence;
		}

		public void setConfidence(String confidence) {
			this.confidence = confidence;
		}

		public String getInternalScore() {
			return internalScore;
		}

		public void setInternalScore(String internalScore) {
			this.internalScore = internalScore;
		}

		public String getKey1() {
			return key1;
		}

		public void setKey1(String key1) {
			this.key1 = key1;
		}

		public String getKey2() {
			return key2;
		}

		public void setKey2(String key2) {
			this.key2 = key2;
		}
	}

	public static class Candidates {
		private String referenceId;
		private Analytics analytics;
		private List<Modalities> modalities;

		public Candidates() {
			super();
		}

		public Candidates(String referenceId, Analytics analytics, List<Modalities> modalities) {
			super();
			this.referenceId = referenceId;
			this.analytics = analytics;
			this.modalities = modalities;
		}

		public String getReferenceId() {
			return referenceId;
		}

		public void setReferenceId(String referenceId) {
			this.referenceId = referenceId;
		}

		public Analytics getAnalytics() {
			return analytics;
		}

		public void setAnalytics(Analytics analytics) {
			this.analytics = analytics;
		}

		public List<Modalities> getModalities() {
			return modalities;
		}

		public void setModalities(List<Modalities> modalities) {
			this.modalities = modalities;
		}
	}
}
