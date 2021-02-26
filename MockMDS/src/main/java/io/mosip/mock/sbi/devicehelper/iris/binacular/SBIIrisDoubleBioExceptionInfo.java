package io.mosip.mock.sbi.devicehelper.iris.binacular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;

public class SBIIrisDoubleBioExceptionInfo extends SBIBioExceptionInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIIrisDoubleBioExceptionInfo.class);	
	
	private SBICheckState chkMissingLeftIris;
	private SBICheckState chkMissingRightIris;
     
	protected SBIIrisDoubleBioExceptionInfo()  
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
				if (tag.trim ().equals (SBIConstant.BIO_NAME_LEFT_IRIS))
                {
					setChkMissingLeftIris (SBICheckState.Checked);
                }
                else if (tag.trim ().equals (SBIConstant.BIO_NAME_RIGHT_IRIS))
                {
                	setChkMissingRightIris (SBICheckState.Checked);
                }
			}
		}
	}

	@Override
	public void deInitBioException() {
		setChkMissingLeftIris (SBICheckState.Unchecked);
		setChkMissingRightIris (SBICheckState.Unchecked);
	}

	public SBICheckState getChkMissingLeftIris() {
		return chkMissingLeftIris;
	}

	public void setChkMissingLeftIris(SBICheckState chkMissingLeftIris) {
		this.chkMissingLeftIris = chkMissingLeftIris;
	}

	public SBICheckState getChkMissingRightIris() {
		return chkMissingRightIris;
	}

	public void setChkMissingRightIris(SBICheckState chkMissingRightIris) {
		this.chkMissingRightIris = chkMissingRightIris;
	}	
}
