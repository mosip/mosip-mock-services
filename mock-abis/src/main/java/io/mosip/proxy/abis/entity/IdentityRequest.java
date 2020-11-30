package io.mosip.proxy.abis.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IdentityRequest {

	private String id;

	private String version;

	private String requestId;

	private LocalDateTime requesttime;

	private String referenceId;

	private String referenceUrl;

	private Gallery gallery=null;

	private Flags flags;

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

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public LocalDateTime getRequesttime() {
		return requesttime;
	}

	public void setRequesttime(LocalDateTime requesttime) {
		this.requesttime = requesttime;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getReferenceUrl() {
		return referenceUrl;
	}

	public void setReferenceUrl(String referenceUrl) {
		this.referenceUrl = referenceUrl;
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public Flags getFlags() {
		return flags;
	}

	public void setFlags(Flags flags) {
		this.flags = flags;
	}

	public IdentityRequest() {
		super();
	}

	public IdentityRequest(String id, String version, String requestId, LocalDateTime requesttime, String referenceId,
			String referenceUrl, Gallery gallery, Flags flags) {
		super();
		this.id = id;
		this.version = version;
		this.requestId = requestId;
		this.requesttime = requesttime;
		this.referenceId = referenceId;
		this.referenceUrl = referenceUrl;
		this.gallery = gallery;
		this.flags = flags;
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
