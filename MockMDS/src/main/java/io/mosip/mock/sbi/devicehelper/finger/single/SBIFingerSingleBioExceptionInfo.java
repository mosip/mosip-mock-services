package io.mosip.mock.sbi.devicehelper.finger.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;

public class SBIFingerSingleBioExceptionInfo extends SBIBioExceptionInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFingerSingleBioExceptionInfo.class);	
	
	private SBICheckState chkMissingLeftIndex;
	private SBICheckState chkMissingLeftMiddle;
	private SBICheckState chkMissingLeftRing;
	private SBICheckState chkMissingLeftLittle;

	private SBICheckState chkMissingRightIndex;
	private SBICheckState chkMissingRightMiddle;
	private SBICheckState chkMissingRightRing;
	private SBICheckState chkMissingRightLittle;

	private SBICheckState chkMissingRightThumb;
	private SBICheckState chkMissingLeftThumb;
     
	protected SBIFingerSingleBioExceptionInfo()  
	{ 
		super ();
		deInitBioException();
	}

	@Override
	public void initBioException(String[] bioException) {
		// TODO Auto-generated method stub
		if (bioException != null && bioException.length > 0)
		{
			for (String tag : bioException){
				switch (tag.trim ())
				{
					case SBIConstant.BIO_NAME_RIGHT_THUMB:
						setChkMissingRightThumb (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_RIGHT_INDEX:
	                	setChkMissingRightIndex (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_RIGHT_MIDDLE:
	                	setChkMissingRightMiddle (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_RIGHT_RING:
	                	setChkMissingRightRing (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_RIGHT_LITTLE:
	                	setChkMissingRightLittle (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_LEFT_THUMB:
	                	setChkMissingLeftThumb (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_LEFT_INDEX:
	                	setChkMissingLeftIndex (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_LEFT_MIDDLE:
	                	setChkMissingLeftMiddle (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_LEFT_RING:
	                	setChkMissingLeftRing (SBICheckState.Checked);
						break;
	                case SBIConstant.BIO_NAME_LEFT_LITTLE:
	                	setChkMissingLeftLittle (SBICheckState.Checked);
						break;
				}
			}
		}
	}

	@Override
	public void deInitBioException() {
		setChkMissingLeftIndex (SBICheckState.Unchecked);
		setChkMissingLeftMiddle (SBICheckState.Unchecked);
		setChkMissingLeftRing (SBICheckState.Unchecked);
		setChkMissingLeftLittle (SBICheckState.Unchecked);

		setChkMissingRightIndex (SBICheckState.Unchecked);
		setChkMissingRightMiddle (SBICheckState.Unchecked);
		setChkMissingRightRing (SBICheckState.Unchecked);
		setChkMissingRightLittle (SBICheckState.Unchecked);

		setChkMissingRightThumb (SBICheckState.Unchecked);
		setChkMissingLeftThumb (SBICheckState.Unchecked);
	}

	public SBICheckState getChkMissingLeftIndex() {
		return chkMissingLeftIndex;
	}

	public void setChkMissingLeftIndex(SBICheckState chkMissingLeftIndex) {
		this.chkMissingLeftIndex = chkMissingLeftIndex;
	}

	public SBICheckState getChkMissingLeftMiddle() {
		return chkMissingLeftMiddle;
	}

	public void setChkMissingLeftMiddle(SBICheckState chkMissingLeftMiddle) {
		this.chkMissingLeftMiddle = chkMissingLeftMiddle;
	}

	public SBICheckState getChkMissingLeftRing() {
		return chkMissingLeftRing;
	}

	public void setChkMissingLeftRing(SBICheckState chkMissingLeftRing) {
		this.chkMissingLeftRing = chkMissingLeftRing;
	}

	public SBICheckState getChkMissingLeftLittle() {
		return chkMissingLeftLittle;
	}

	public void setChkMissingLeftLittle(SBICheckState chkMissingLeftLittle) {
		this.chkMissingLeftLittle = chkMissingLeftLittle;
	}

	public SBICheckState getChkMissingRightIndex() {
		return chkMissingRightIndex;
	}

	public void setChkMissingRightIndex(SBICheckState chkMissingRightIndex) {
		this.chkMissingRightIndex = chkMissingRightIndex;
	}

	public SBICheckState getChkMissingRightMiddle() {
		return chkMissingRightMiddle;
	}

	public void setChkMissingRightMiddle(SBICheckState chkMissingRightMiddle) {
		this.chkMissingRightMiddle = chkMissingRightMiddle;
	}

	public SBICheckState getChkMissingRightRing() {
		return chkMissingRightRing;
	}

	public void setChkMissingRightRing(SBICheckState chkMissingRightRing) {
		this.chkMissingRightRing = chkMissingRightRing;
	}

	public SBICheckState getChkMissingRightLittle() {
		return chkMissingRightLittle;
	}

	public void setChkMissingRightLittle(SBICheckState chkMissingRightLittle) {
		this.chkMissingRightLittle = chkMissingRightLittle;
	}

	public SBICheckState getChkMissingRightThumb() {
		return chkMissingRightThumb;
	}

	public void setChkMissingRightThumb(SBICheckState chkMissingRightThumb) {
		this.chkMissingRightThumb = chkMissingRightThumb;
	}

	public SBICheckState getChkMissingLeftThumb() {
		return chkMissingLeftThumb;
	}

	public void setChkMissingLeftThumb(SBICheckState chkMissingLeftThumb) {
		this.chkMissingLeftThumb = chkMissingLeftThumb;
	} 	
}
