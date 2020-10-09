package io.mosip.mock.sbi.devicehelper;

public abstract class SBICaptureInfo {
	private byte[] image;
    private boolean liveStreamStarted = false;
    private boolean liveStreamCompleted = false;
    private boolean captureStarted = false;
    private boolean captureCompleted = false;
    private SBIBioExceptionInfo bioExceptionInfo = null;
    public abstract void initCaptureInfo ();
	public abstract void deInitCaptureInfo ();
	private int requestScore;
		
	public SBICaptureInfo() {
		super();
	}
		
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}

	public boolean isLiveStreamStarted() {
		return liveStreamStarted;
	}
	public void setLiveStreamStarted(boolean liveStreamStarted) {
		this.liveStreamStarted = liveStreamStarted;
	}
	
	public boolean isLiveStreamCompleted() {
		return liveStreamCompleted;
	}
	public void setLiveStreamCompleted(boolean liveStreamCompleted) {
		this.liveStreamCompleted = liveStreamCompleted;
	}
	
	public boolean isCaptureStarted() {
		return captureStarted;
	}
	public void setCaptureStarted(boolean captureStarted) {
		this.captureStarted = captureStarted;
	}

	public boolean isCaptureCompleted() {
		return captureCompleted;
	}
	public void setCaptureCompleted(boolean captureCompleted) {
		this.captureCompleted = captureCompleted;
	}
	
	public SBIBioExceptionInfo getBioExceptionInfo() {
		return bioExceptionInfo;
	}
	public void setBioExceptionInfo(SBIBioExceptionInfo bioExceptionInfo) {
		this.bioExceptionInfo = bioExceptionInfo;
	}
	public int getRequestScore() {
		return requestScore;
	}
	public void setRequestScore(int requestScore) {
		this.requestScore = requestScore;
	}		
}
