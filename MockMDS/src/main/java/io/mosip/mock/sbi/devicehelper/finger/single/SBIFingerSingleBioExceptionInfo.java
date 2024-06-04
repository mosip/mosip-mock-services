package io.mosip.mock.sbi.devicehelper.finger.single;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;

public class SBIFingerSingleBioExceptionInfo extends SBIBioExceptionInfo {
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

	protected SBIFingerSingleBioExceptionInfo() {
		super();
		deInitBioException();
	}

	@Override
	public void initBioException(String[] bioException) {
		if (bioException != null && bioException.length > 0) {
			for (String tag : bioException) {
				switch (tag.trim()) {
				case SBIConstant.BIO_NAME_RIGHT_THUMB:
					setChkMissingRightThumb(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_INDEX:
					setChkMissingRightIndex(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_MIDDLE:
					setChkMissingRightMiddle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_RING:
					setChkMissingRightRing(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_LITTLE:
					setChkMissingRightLittle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_THUMB:
					setChkMissingLeftThumb(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_INDEX:
					setChkMissingLeftIndex(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_MIDDLE:
					setChkMissingLeftMiddle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_RING:
					setChkMissingLeftRing(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_LITTLE:
					setChkMissingLeftLittle(SBICheckState.CHECKED);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void deInitBioException() {
		setChkMissingLeftIndex(SBICheckState.UNCHECKED);
		setChkMissingLeftMiddle(SBICheckState.UNCHECKED);
		setChkMissingLeftRing(SBICheckState.UNCHECKED);
		setChkMissingLeftLittle(SBICheckState.UNCHECKED);

		setChkMissingRightIndex(SBICheckState.UNCHECKED);
		setChkMissingRightMiddle(SBICheckState.UNCHECKED);
		setChkMissingRightRing(SBICheckState.UNCHECKED);
		setChkMissingRightLittle(SBICheckState.UNCHECKED);

		setChkMissingRightThumb(SBICheckState.UNCHECKED);
		setChkMissingLeftThumb(SBICheckState.UNCHECKED);
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