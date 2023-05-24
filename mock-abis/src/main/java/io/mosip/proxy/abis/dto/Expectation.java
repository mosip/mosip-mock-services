package io.mosip.proxy.abis.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Expectation {
	private String id;
	private String version;
	private LocalDateTime requesttime;
	/* Insert/ Identity */
	private String actionToInterfere;
	/* Error/ Duplicate/ Success (default) */
	private String forcedResponse;
	/* Failure response */
	private String errorCode;
	private String delayInExecution;
	private Gallery matchedGallery;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public LocalDateTime getRequesttime() {
		return requesttime;
	}

	public void setRequesttime(LocalDateTime requesttime) {
		this.requesttime = requesttime;
	}

	public String getActionToInterfere() {
		return actionToInterfere;
	}

	public void setActionToInterfere(String actionToInterfere) {
		this.actionToInterfere = actionToInterfere;
	}

	public String getForcedResponse() {
		return forcedResponse;
	}

	public void setForcedResponse(String forcedResponse) {
		this.forcedResponse = forcedResponse;
	}

	public Gallery getGallery() {
		return matchedGallery;
	}

	public void setGallery(Gallery matchedGallery) {
		this.matchedGallery = matchedGallery;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String e) {
		this.errorCode = e;
	}

	public String getDelayInExecution() {
		return delayInExecution;
	}

	public void setDelayInExecution(String e) {
		this.delayInExecution = e;
	}

	public Expectation() {
		super();
	}

	public Expectation(String id, String version, LocalDateTime requesttime, String actionToInterfere,
			String forcedResponse, Gallery matchedGallery) {
		super();
		this.id = id;
		this.version = version;
		this.requesttime = requesttime;
		this.actionToInterfere = actionToInterfere;
		this.forcedResponse = forcedResponse;
		this.matchedGallery = matchedGallery;
	}

	public static class Gallery {
		List<ReferenceIds> referenceIds = new ArrayList<>();
		public Gallery() {

		}

		public Gallery(List<ReferenceIds> referenceIds) {
			super();
			this.referenceIds = referenceIds;
		}

		public List<ReferenceIds> getReferenceIds() {
			return referenceIds;
		}
		public void setReferenceIds(List<ReferenceIds> referenceIds) {
			this.referenceIds = referenceIds;
		}
	}

	public static class ReferenceIds {
		private String referenceId;

		public ReferenceIds() {
			super();
		}

		public ReferenceIds(String referenceId) {
			super();
			this.referenceId = referenceId;
		}

		public String getReferenceId() {
			return referenceId;
		}
		public void setReferenceId(String referenceId) {
			this.referenceId = referenceId;
		}
	}

	public static class Flags {
		private String maxResults;
		private String targetFPIR;
		private String flag1;
		private String flag2;

		public Flags() {
			super();
		}

		public Flags(String maxResults, String targetFPIR, String flag1, String flag2) {
			super();
			this.maxResults = maxResults;
			this.targetFPIR = targetFPIR;
			this.flag1 = flag1;
			this.flag2 = flag2;
		}

		public String getMaxResults() {
			return maxResults;
		}
		public void setMaxResults(String maxResults) {
			this.maxResults = maxResults;
		}

		public String getTargetFPIR() {
			return targetFPIR;
		}
		public void setTargetFPIR(String targetFPIR) {
			this.targetFPIR = targetFPIR;
		}

		public String getFlag1() {
			return flag1;
		}

		public void setFlag1(String flag1) {
			this.flag1 = flag1;
		}

		public String getFlag2() {
			return flag2;
		}

		public void setFlag2(String flag2) {
			this.flag2 = flag2;
		}
	}
}