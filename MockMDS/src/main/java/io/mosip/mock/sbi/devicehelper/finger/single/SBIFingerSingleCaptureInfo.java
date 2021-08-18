package io.mosip.mock.sbi.devicehelper.finger.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;

public class SBIFingerSingleCaptureInfo extends SBICaptureInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFingerSingleCaptureInfo.class);	
	
	private String bioValueLT;
	private String bioSubTypeLT;
	private int requestScoreLT;
	private int captureScoreLT;
	private boolean isCaptureLT;
	
	private String bioValueLI;
	private String bioSubTypeLI;
	private int requestScoreLI;
	private int captureScoreLI;
	private boolean isCaptureLI;
	
	private String bioValueLM;
	private String bioSubTypeLM;
	private int requestScoreLM;
	private int captureScoreLM;
	private boolean isCaptureLM;
	
	private String bioValueLR;
	private String bioSubTypeLR;
	private int requestScoreLR;
	private int captureScoreLR;
	private boolean isCaptureLR;
	
	private String bioValueLL;
	private String bioSubTypeLL;
	private int requestScoreLL;
	private int captureScoreLL;
	private boolean isCaptureLL;
	
	private String bioValueRT;
	private String bioSubTypeRT;
	private int requestScoreRT;
	private int captureScoreRT;
	private boolean isCaptureRT;
	
	private String bioValueRI;
	private String bioSubTypeRI;
	private int requestScoreRI;
	private int captureScoreRI;
	private boolean isCaptureRI;
	
	private String bioValueRM;
	private String bioSubTypeRM;
	private int requestScoreRM;
	private int captureScoreRM;
	private boolean isCaptureRM;
	
	private String bioValueRR;
	private String bioSubTypeRR;
	private int requestScoreRR;
	private int captureScoreRR;
	private boolean isCaptureRR;
	
	private String bioValueRL;
	private String bioSubTypeRL;
	private int requestScoreRL;
	private int captureScoreRL;
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

	public int getRequestScoreLT() {
		return requestScoreLT;
	}

	public void setRequestScoreLT(int requestScoreLT) {
		this.requestScoreLT = requestScoreLT;
	}

	public int getCaptureScoreLT() {
		return captureScoreLT;
	}

	public void setCaptureScoreLT(int captureScoreLT) {
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

	public int getRequestScoreLI() {
		return requestScoreLI;
	}

	public void setRequestScoreLI(int requestScoreLI) {
		this.requestScoreLI = requestScoreLI;
	}

	public int getCaptureScoreLI() {
		return captureScoreLI;
	}

	public void setCaptureScoreLI(int captureScoreLI) {
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

	public int getRequestScoreLM() {
		return requestScoreLM;
	}

	public void setRequestScoreLM(int requestScoreLM) {
		this.requestScoreLM = requestScoreLM;
	}

	public int getCaptureScoreLM() {
		return captureScoreLM;
	}

	public void setCaptureScoreLM(int captureScoreLM) {
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

	public int getRequestScoreLR() {
		return requestScoreLR;
	}

	public void setRequestScoreLR(int requestScoreLR) {
		this.requestScoreLR = requestScoreLR;
	}

	public int getCaptureScoreLR() {
		return captureScoreLR;
	}

	public void setCaptureScoreLR(int captureScoreLR) {
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

	public int getRequestScoreLL() {
		return requestScoreLL;
	}

	public void setRequestScoreLL(int requestScoreLL) {
		this.requestScoreLL = requestScoreLL;
	}

	public int getCaptureScoreLL() {
		return captureScoreLL;
	}

	public void setCaptureScoreLL(int captureScoreLL) {
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

	public int getRequestScoreRT() {
		return requestScoreRT;
	}

	public void setRequestScoreRT(int requestScoreRT) {
		this.requestScoreRT = requestScoreRT;
	}

	public int getCaptureScoreRT() {
		return captureScoreRT;
	}

	public void setCaptureScoreRT(int captureScoreRT) {
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

	public int getRequestScoreRI() {
		return requestScoreRI;
	}

	public void setRequestScoreRI(int requestScoreRI) {
		this.requestScoreRI = requestScoreRI;
	}

	public int getCaptureScoreRI() {
		return captureScoreRI;
	}

	public void setCaptureScoreRI(int captureScoreRI) {
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

	public int getRequestScoreRM() {
		return requestScoreRM;
	}

	public void setRequestScoreRM(int requestScoreRM) {
		this.requestScoreRM = requestScoreRM;
	}

	public int getCaptureScoreRM() {
		return captureScoreRM;
	}

	public void setCaptureScoreRM(int captureScoreRM) {
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

	public int getRequestScoreRR() {
		return requestScoreRR;
	}

	public void setRequestScoreRR(int requestScoreRR) {
		this.requestScoreRR = requestScoreRR;
	}

	public int getCaptureScoreRR() {
		return captureScoreRR;
	}

	public void setCaptureScoreRR(int captureScoreRR) {
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

	public int getRequestScoreRL() {
		return requestScoreRL;
	}

	public void setRequestScoreRL(int requestScoreRL) {
		this.requestScoreRL = requestScoreRL;
	}

	public int getCaptureScoreRL() {
		return captureScoreRL;
	}

	public void setCaptureScoreRL(int captureScoreRL) {
		this.captureScoreRL = captureScoreRL;
	}

	public boolean isCaptureRL() {
		return isCaptureRL;
	}

	public void setCaptureRL(boolean isCaptureRL) {
		this.isCaptureRL = isCaptureRL;
	}	
}
