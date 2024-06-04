package io.mosip.mock.sbi.devicehelper.iris.binacular;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;

public class SBIIrisDoubleBioExceptionInfo extends SBIBioExceptionInfo {
	private SBICheckState chkMissingLeftIris;
	private SBICheckState chkMissingRightIris;

	protected SBIIrisDoubleBioExceptionInfo() {
		super();
		deInitBioException();
	}

	@Override
	public void initBioException(String[] bioException) {
		if (bioException != null && bioException.length > 0) {
			for (String tag : bioException) {
				switch (tag.trim()) {
				case SBIConstant.BIO_NAME_LEFT_IRIS:
					setChkMissingLeftIris(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_IRIS:
					setChkMissingRightIris(SBICheckState.CHECKED);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void deInitBioException() {
		setChkMissingLeftIris(SBICheckState.UNCHECKED);
		setChkMissingRightIris(SBICheckState.UNCHECKED);
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