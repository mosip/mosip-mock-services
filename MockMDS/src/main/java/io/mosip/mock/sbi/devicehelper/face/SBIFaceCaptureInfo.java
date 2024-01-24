package io.mosip.mock.sbi.devicehelper.face;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;

public class SBIFaceCaptureInfo extends SBICaptureInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFaceCaptureInfo.class);	
	
	private String bioValueFace;
	private String bioSubTypeFace;
	private float requestScoreFace;
	private float captureScoreFace;
	private boolean isCaptureFace;
	
	private String bioValueExceptionPhoto;
	private String bioSubTypeExceptionPhoto;
	private int requestScoreExceptionPhoto;
	private int captureScoreExceptionPhoto;
	private boolean isCaptureExceptionPhoto;
	     
	public SBIFaceCaptureInfo()  
	{ 
		super ();
	} 
  
	@Override
	public void initCaptureInfo() {
		setImage (null);
		setLiveStreamStarted (false);
		setLiveStreamCompleted (false);
		setCaptureStarted (false);
		setCaptureCompleted (false);	
		setBioExceptionInfo (null);
	}

	@Override
	public void deInitCaptureInfo() {
		setImage (null);
		setLiveStreamStarted (false);
		setLiveStreamCompleted (false);
		setCaptureStarted (false);
		setCaptureCompleted (false);
		setBioExceptionInfo (null);
	}

	public String getBioValueFace() {
		return bioValueFace;
	}

	public void setBioValueFace(String bioValueFace) {
		this.bioValueFace = bioValueFace;
	}

	public String getBioSubTypeFace() {
		return bioSubTypeFace;
	}

	public void setBioSubTypeFace(String bioSubTypeFace) {
		this.bioSubTypeFace = bioSubTypeFace;
	}

	public float getRequestScoreFace() {
		return requestScoreFace;
	}

	public void setRequestScoreFace(float requestScoreFace) {
		this.requestScoreFace = requestScoreFace;
	}

	public float getCaptureScoreFace() {
		return captureScoreFace;
	}

	public void setCaptureScoreFace(float captureScoreFace) {
		this.captureScoreFace = captureScoreFace;
	}

	public boolean isCaptureFace() {
		return isCaptureFace;
	}

	public void setCaptureFace(boolean isCaptureFace) {
		this.isCaptureFace = isCaptureFace;
	}

	public String getBioValueExceptionPhoto() {
		return bioValueExceptionPhoto;
	}

	public void setBioValueExceptionPhoto(String bioValueExceptionPhoto) {
		this.bioValueExceptionPhoto = bioValueExceptionPhoto;
	}

	public String getBioSubTypeExceptionPhoto() {
		return bioSubTypeExceptionPhoto;
	}

	public void setBioSubTypeExceptionPhoto(String bioSubTypeExceptionPhoto) {
		this.bioSubTypeExceptionPhoto = bioSubTypeExceptionPhoto;
	}

	public int getRequestScoreExceptionPhoto() {
		return requestScoreExceptionPhoto;
	}

	public void setRequestScoreExceptionPhoto(int requestScoreExceptionPhoto) {
		this.requestScoreExceptionPhoto = requestScoreExceptionPhoto;
	}

	public int getCaptureScoreExceptionPhoto() {
		return captureScoreExceptionPhoto;
	}

	public void setCaptureScoreExceptionPhoto(int captureScoreExceptionPhoto) {
		this.captureScoreExceptionPhoto = captureScoreExceptionPhoto;
	}

	public boolean isCaptureExceptionPhoto() {
		return isCaptureExceptionPhoto;
	}

	public void setCaptureExceptionPhoto(boolean isCaptureExceptionPhoto) {
		this.isCaptureExceptionPhoto = isCaptureExceptionPhoto;
	}	
}
