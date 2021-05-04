package io.mosip.proxy.abis.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Expectation {

	private String id;

	private String version;

	private LocalDateTime requesttime;

	private String actionToInterfere;

	private String forcedResponse;

	private Gallery matchedGallery=null;

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

		private Integer maxResults;
		private Integer targetFPIR;
		private String flag1;
		private String flag2;

		public Flags() {
			super();
		}

		public Flags(Integer maxResults, Integer targetFPIR, String flag1, String flag2) {
			super();
			this.maxResults = maxResults;
			this.targetFPIR = targetFPIR;
			this.flag1 = flag1;
			this.flag2 = flag2;
		}

		public Integer getMaxResults() {
			return maxResults;
		}

		public void setMaxResults(Integer maxResults) {
			this.maxResults = maxResults;
		}

		public Integer getTargetFPIR() {
			return targetFPIR;
		}

		public void setTargetFPIR(Integer targetFPIR) {
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
