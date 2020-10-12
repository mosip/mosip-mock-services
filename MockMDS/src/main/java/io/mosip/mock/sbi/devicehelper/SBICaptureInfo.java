package io.mosip.mock.sbi.devicehelper;

import java.util.HashMap;

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
	private String[] bioSubType;
	private int bioCount; //Finger (10)/Iris count(2), in case of face max is set to 1
    private HashMap<String, String> biometricData = new HashMap<> ();
		
	public SBICaptureInfo() {
		super();
		setBiometricData (new HashMap<> ());
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
	public String[] getBioSubType() {
		return bioSubType;
	}
	public void setBioSubType(String[] bioSubType) {
		this.bioSubType = bioSubType;
	}
	public int getBioCount() {
		return bioCount;
	}
	public void setBioCount(int bioCount) {
		this.bioCount = bioCount;
	}		

	public HashMap<String, String> getBiometricData() {
		return biometricData;
	}
	public void setBiometricData(HashMap<String, String> biometricData) {
		this.biometricData = biometricData;
	}		
	
	public void addBiometricForBioSubType(String bioSubType, String bioValue) {
		if (getBiometricData() == null)
			setBiometricData (new HashMap<> ());
		
		getBiometricData().putIfAbsent(bioSubType, bioValue);
	}	

	public String getBiometricForBioSubType(String bioSubType) {
		if (getBiometricData() == null)
			return null;
		
		return getBiometricData().get(bioSubType);
	}	
}
