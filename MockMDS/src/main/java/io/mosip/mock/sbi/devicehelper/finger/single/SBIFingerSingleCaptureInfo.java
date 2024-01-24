package io.mosip.mock.sbi.devicehelper.finger.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;

public class SBIFingerSingleCaptureInfo extends SBICaptureInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFingerSingleCaptureInfo.class);	
	
	private String bioValueLT;
	private String bioSubTypeLT;
	private float requestScoreLT;
	private float captureScoreLT;
	private boolean isCaptureLT;
	
	private String bioValueLI;
	private String bioSubTypeLI;
	private float requestScoreLI;
	private float captureScoreLI;
	private boolean isCaptureLI;
	
	private String bioValueLM;
	private String bioSubTypeLM;
	private float requestScoreLM;
	private float captureScoreLM;
	private boolean isCaptureLM;
	
	private String bioValueLR;
	private String bioSubTypeLR;
	private float requestScoreLR;
	private float captureScoreLR;
	private boolean isCaptureLR;
	
	private String bioValueLL;
	private String bioSubTypeLL;
	private float requestScoreLL;
	private float captureScoreLL;
	private boolean isCaptureLL;
	
	private String bioValueRT;
	private String bioSubTypeRT;
	private float requestScoreRT;
	private float captureScoreRT;
	private boolean isCaptureRT;
	
	private String bioValueRI;
	private String bioSubTypeRI;
	private float requestScoreRI;
	private float captureScoreRI;
	private boolean isCaptureRI;
	
	private String bioValueRM;
	private String bioSubTypeRM;
	private float requestScoreRM;
	private float captureScoreRM;
	private boolean isCaptureRM;
	
	private String bioValueRR;
	private String bioSubTypeRR;
	private float requestScoreRR;
	private float captureScoreRR;
	private boolean isCaptureRR;
	
	private String bioValueRL;
	private String bioSubTypeRL;
	private float requestScoreRL;
	private float captureScoreRL;
	private boolean isCaptureRL;
     
	public SBIFingerSingleCaptureInfo()  
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
		setBioExceptionInfo (new SBIFingerSingleBioExceptionInfo ());
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

	public String getBioValueLT() {
		return bioValueLT;
	}

	public void setBioValueLT(String bioValueLT) {
		this.bioValueLT = bioValueLT;
	}

	public String getBioSubTypeLT() {
		return bioSubTypeLT;
	}

	public void setBioSubTypeLT(String bioSubTypeLT) {
		this.bioSubTypeLT = bioSubTypeLT;
	}

	public float getRequestScoreLT() {
		return requestScoreLT;
	}

	public void setRequestScoreLT(float requestScoreLT) {
		this.requestScoreLT = requestScoreLT;
	}

	public float getCaptureScoreLT() {
		return captureScoreLT;
	}

	public void setCaptureScoreLT(float captureScoreLT) {
		this.captureScoreLT = captureScoreLT;
	}

	public boolean isCaptureLT() {
		return isCaptureLT;
	}

	public void setCaptureLT(boolean isCaptureLT) {
		this.isCaptureLT = isCaptureLT;
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

	public String getBioValueLM() {
		return bioValueLM;
	}

	public void setBioValueLM(String bioValueLM) {
		this.bioValueLM = bioValueLM;
	}

	public String getBioSubTypeLM() {
		return bioSubTypeLM;
	}

	public void setBioSubTypeLM(String bioSubTypeLM) {
		this.bioSubTypeLM = bioSubTypeLM;
	}

	public float getRequestScoreLM() {
		return requestScoreLM;
	}

	public void setRequestScoreLM(float requestScoreLM) {
		this.requestScoreLM = requestScoreLM;
	}

	public float getCaptureScoreLM() {
		return captureScoreLM;
	}

	public void setCaptureScoreLM(float captureScoreLM) {
		this.captureScoreLM = captureScoreLM;
	}

	public boolean isCaptureLM() {
		return isCaptureLM;
	}

	public void setCaptureLM(boolean isCaptureLM) {
		this.isCaptureLM = isCaptureLM;
	}

	public String getBioValueLR() {
		return bioValueLR;
	}

	public void setBioValueLR(String bioValueLR) {
		this.bioValueLR = bioValueLR;
	}

	public String getBioSubTypeLR() {
		return bioSubTypeLR;
	}

	public void setBioSubTypeLR(String bioSubTypeLR) {
		this.bioSubTypeLR = bioSubTypeLR;
	}

	public float getRequestScoreLR() {
		return requestScoreLR;
	}

	public void setRequestScoreLR(float requestScoreLR) {
		this.requestScoreLR = requestScoreLR;
	}

	public float getCaptureScoreLR() {
		return captureScoreLR;
	}

	public void setCaptureScoreLR(float captureScoreLR) {
		this.captureScoreLR = captureScoreLR;
	}

	public boolean isCaptureLR() {
		return isCaptureLR;
	}

	public void setCaptureLR(boolean isCaptureLR) {
		this.isCaptureLR = isCaptureLR;
	}

	public String getBioValueLL() {
		return bioValueLL;
	}

	public void setBioValueLL(String bioValueLL) {
		this.bioValueLL = bioValueLL;
	}

	public String getBioSubTypeLL() {
		return bioSubTypeLL;
	}

	public void setBioSubTypeLL(String bioSubTypeLL) {
		this.bioSubTypeLL = bioSubTypeLL;
	}

	public float getRequestScoreLL() {
		return requestScoreLL;
	}

	public void setRequestScoreLL(float requestScoreLL) {
		this.requestScoreLL = requestScoreLL;
	}

	public float getCaptureScoreLL() {
		return captureScoreLL;
	}

	public void setCaptureScoreLL(float captureScoreLL) {
		this.captureScoreLL = captureScoreLL;
	}

	public boolean isCaptureLL() {
		return isCaptureLL;
	}

	public void setCaptureLL(boolean isCaptureLL) {
		this.isCaptureLL = isCaptureLL;
	}

	public String getBioValueRT() {
		return bioValueRT;
	}

	public void setBioValueRT(String bioValueRT) {
		this.bioValueRT = bioValueRT;
	}

	public String getBioSubTypeRT() {
		return bioSubTypeRT;
	}

	public void setBioSubTypeRT(String bioSubTypeRT) {
		this.bioSubTypeRT = bioSubTypeRT;
	}

	public float getRequestScoreRT() {
		return requestScoreRT;
	}

	public void setRequestScoreRT(float requestScoreRT) {
		this.requestScoreRT = requestScoreRT;
	}

	public float getCaptureScoreRT() {
		return captureScoreRT;
	}

	public void setCaptureScoreRT(float captureScoreRT) {
		this.captureScoreRT = captureScoreRT;
	}

	public boolean isCaptureRT() {
		return isCaptureRT;
	}

	public void setCaptureRT(boolean isCaptureRT) {
		this.isCaptureRT = isCaptureRT;
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

	public String getBioValueRM() {
		return bioValueRM;
	}

	public void setBioValueRM(String bioValueRM) {
		this.bioValueRM = bioValueRM;
	}

	public String getBioSubTypeRM() {
		return bioSubTypeRM;
	}

	public void setBioSubTypeRM(String bioSubTypeRM) {
		this.bioSubTypeRM = bioSubTypeRM;
	}

	public float getRequestScoreRM() {
		return requestScoreRM;
	}

	public void setRequestScoreRM(float requestScoreRM) {
		this.requestScoreRM = requestScoreRM;
	}

	public float getCaptureScoreRM() {
		return captureScoreRM;
	}

	public void setCaptureScoreRM(float captureScoreRM) {
		this.captureScoreRM = captureScoreRM;
	}

	public boolean isCaptureRM() {
		return isCaptureRM;
	}

	public void setCaptureRM(boolean isCaptureRM) {
		this.isCaptureRM = isCaptureRM;
	}

	public String getBioValueRR() {
		return bioValueRR;
	}

	public void setBioValueRR(String bioValueRR) {
		this.bioValueRR = bioValueRR;
	}

	public String getBioSubTypeRR() {
		return bioSubTypeRR;
	}

	public void setBioSubTypeRR(String bioSubTypeRR) {
		this.bioSubTypeRR = bioSubTypeRR;
	}

	public float getRequestScoreRR() {
		return requestScoreRR;
	}

	public void setRequestScoreRR(float requestScoreRR) {
		this.requestScoreRR = requestScoreRR;
	}

	public float getCaptureScoreRR() {
		return captureScoreRR;
	}

	public void setCaptureScoreRR(float captureScoreRR) {
		this.captureScoreRR = captureScoreRR;
	}

	public boolean isCaptureRR() {
		return isCaptureRR;
	}

	public void setCaptureRR(boolean isCaptureRR) {
		this.isCaptureRR = isCaptureRR;
	}

	public String getBioValueRL() {
		return bioValueRL;
	}

	public void setBioValueRL(String bioValueRL) {
		this.bioValueRL = bioValueRL;
	}

	public String getBioSubTypeRL() {
		return bioSubTypeRL;
	}

	public void setBioSubTypeRL(String bioSubTypeRL) {
		this.bioSubTypeRL = bioSubTypeRL;
	}

	public float getRequestScoreRL() {
		return requestScoreRL;
	}

	public void setRequestScoreRL(float requestScoreRL) {
		this.requestScoreRL = requestScoreRL;
	}

	public float getCaptureScoreRL() {
		return captureScoreRL;
	}

	public void setCaptureScoreRL(float captureScoreRL) {
		this.captureScoreRL = captureScoreRL;
	}

	public boolean isCaptureRL() {
		return isCaptureRL;
	}

	public void setCaptureRL(boolean isCaptureRL) {
		this.isCaptureRL = isCaptureRL;
	}	
}
