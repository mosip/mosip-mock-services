package io.mosip.mock.sbi.devicehelper;

import io.mosip.mock.sbi.SBIConstant;

public class SBIBioSubTypeInfo {
	private SBICheckState chkUnknown;
	private SBICheckState chkLeftIndex;
	private SBICheckState chkLeftMiddle;
	private SBICheckState chkLeftRing;
	private SBICheckState chkLeftLittle;

	private SBICheckState chkRightIndex;
	private SBICheckState chkRightMiddle;
	private SBICheckState chkRightRing;
	private SBICheckState chkRightLittle;

	private SBICheckState chkRightThumb;
	private SBICheckState chkLeftThumb;

	private SBICheckState chkRightIris;
	private SBICheckState chkLeftIris;

	public SBIBioSubTypeInfo() {
		super();
	}

	public void initBioSubType(String[] bioSubType) {
		if (bioSubType != null && bioSubType.length > 0) {
			for (String tag : bioSubType) {
				switch (tag.trim()) {
				case SBIConstant.BIO_NAME_UNKNOWN:
					setChkUnknown(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_THUMB:
					setChkRightThumb(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_INDEX:
					setChkRightIndex(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_MIDDLE:
					setChkRightMiddle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_RING:
					setChkRightRing(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_LITTLE:
					setChkRightLittle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_THUMB:
					setChkLeftThumb(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_INDEX:
					setChkLeftIndex(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_MIDDLE:
					setChkLeftMiddle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_RING:
					setChkLeftRing(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_LITTLE:
					setChkLeftLittle(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_LEFT_IRIS:
					setChkLeftIris(SBICheckState.CHECKED);
					break;
				case SBIConstant.BIO_NAME_RIGHT_IRIS:
					setChkRightIris(SBICheckState.CHECKED);
					break;
				default:
					setChkUnknown(SBICheckState.CHECKED);
					break;
				}
			}
		} else {
			setChkUnknown(SBICheckState.CHECKED);
		}
	}

	public SBICheckState getChkUnknown() {
		return chkUnknown;
	}

	public void setChkUnknown(SBICheckState chkUnknown) {
		this.chkUnknown = chkUnknown;
	}

	public SBICheckState getChkLeftIndex() {
		return chkLeftIndex;
	}

	public void setChkLeftIndex(SBICheckState chkLeftIndex) {
		this.chkLeftIndex = chkLeftIndex;
	}

	public SBICheckState getChkLeftMiddle() {
		return chkLeftMiddle;
	}

	public void setChkLeftMiddle(SBICheckState chkLeftMiddle) {
		this.chkLeftMiddle = chkLeftMiddle;
	}

	public SBICheckState getChkLeftRing() {
		return chkLeftRing;
	}

	public void setChkLeftRing(SBICheckState chkLeftRing) {
		this.chkLeftRing = chkLeftRing;
	}

	public SBICheckState getChkLeftLittle() {
		return chkLeftLittle;
	}

	public void setChkLeftLittle(SBICheckState chkLeftLittle) {
		this.chkLeftLittle = chkLeftLittle;
	}

	public SBICheckState getChkRightIndex() {
		return chkRightIndex;
	}

	public void setChkRightIndex(SBICheckState chkRightIndex) {
		this.chkRightIndex = chkRightIndex;
	}

	public SBICheckState getChkRightMiddle() {
		return chkRightMiddle;
	}

	public void setChkRightMiddle(SBICheckState chkRightMiddle) {
		this.chkRightMiddle = chkRightMiddle;
	}

	public SBICheckState getChkRightRing() {
		return chkRightRing;
	}

	public void setChkRightRing(SBICheckState chkRightRing) {
		this.chkRightRing = chkRightRing;
	}

	public SBICheckState getChkRightLittle() {
		return chkRightLittle;
	}

	public void setChkRightLittle(SBICheckState chkRightLittle) {
		this.chkRightLittle = chkRightLittle;
	}

	public SBICheckState getChkRightThumb() {
		return chkRightThumb;
	}

	public void setChkRightThumb(SBICheckState chkRightThumb) {
		this.chkRightThumb = chkRightThumb;
	}

	public SBICheckState getChkLeftThumb() {
		return chkLeftThumb;
	}

	public void setChkLeftThumb(SBICheckState chkLeftThumb) {
		this.chkLeftThumb = chkLeftThumb;
	}

	public SBICheckState getChkRightIris() {
		return chkRightIris;
	}

	public void setChkRightIris(SBICheckState chkRightIris) {
		this.chkRightIris = chkRightIris;
	}

	public SBICheckState getChkLeftIris() {
		return chkLeftIris;
	}

	public void setChkLeftIris(SBICheckState chkLeftIris) {
		this.chkLeftIris = chkLeftIris;
	}
}