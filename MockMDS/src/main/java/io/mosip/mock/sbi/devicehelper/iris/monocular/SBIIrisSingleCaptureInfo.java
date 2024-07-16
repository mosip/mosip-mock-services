package io.mosip.mock.sbi.devicehelper.iris.monocular;

import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;

public class SBIIrisSingleCaptureInfo extends SBICaptureInfo {
	private String bioValueLI;
	private String bioSubTypeLI;
	private float requestScoreLI;
	private float captureScoreLI;
	private boolean isCaptureLI;

	private String bioValueRI;
	private String bioSubTypeRI;
	private float requestScoreRI;
	private float captureScoreRI;
	private boolean isCaptureRI;

	public SBIIrisSingleCaptureInfo() {
		super();
	}

	@Override
	public void initCaptureInfo() {
		setImage(null);
		setLiveStreamStarted(false);
		setLiveStreamCompleted(false);
		setCaptureStarted(false);
		setCaptureCompleted(false);
		setBioExceptionInfo(new SBIIrisSingleBioExceptionInfo());
	}

	@Override
	public void deInitCaptureInfo() {
		setImage(null);
		setLiveStreamStarted(false);
		setLiveStreamCompleted(false);
		setCaptureStarted(false);
		setCaptureCompleted(false);
		setBioExceptionInfo(null);
	}

	public String getBioValueLI() {
		return bioValueLI;
	}

	public void setBioValueLI(String bioValueLI) {
		this.bioValueLI = bioValueLI;
	}

	public String getBioSubTypeLI() {
		return bioSubTypeLI;
	}

	public void setBioSubTypeLI(String bioSubTypeLI) {
		this.bioSubTypeLI = bioSubTypeLI;
	}

	public float getRequestScoreLI() {
		return requestScoreLI;
	}

	public void setRequestScoreLI(float requestScoreLI) {
		this.requestScoreLI = requestScoreLI;
	}

	public float getCaptureScoreLI() {
		return captureScoreLI;
	}

	public void setCaptureScoreLI(float captureScoreLI) {
		this.captureScoreLI = captureScoreLI;
	}

	public boolean isCaptureLI() {
		return isCaptureLI;
	}

	public void setCaptureLI(boolean isCaptureLI) {
		this.isCaptureLI = isCaptureLI;
	}

	public String getBioValueRI() {
		return bioValueRI;
	}

	public void setBioValueRI(String bioValueRI) {
		this.bioValueRI = bioValueRI;
	}

	public String getBioSubTypeRI() {
		return bioSubTypeRI;
	}

	public void setBioSubTypeRI(String bioSubTypeRI) {
		this.bioSubTypeRI = bioSubTypeRI;
	}

	public float getRequestScoreRI() {
		return requestScoreRI;
	}

	public void setRequestScoreRI(float requestScoreRI) {
		this.requestScoreRI = requestScoreRI;
	}

	public float getCaptureScoreRI() {
		return captureScoreRI;
	}

	public void setCaptureScoreRI(float captureScoreRI) {
		this.captureScoreRI = captureScoreRI;
	}

	public boolean isCaptureRI() {
		return isCaptureRI;
	}

	public void setCaptureRI(boolean isCaptureRI) {
		this.isCaptureRI = isCaptureRI;
	}
}