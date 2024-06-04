package io.mosip.mock.sbi.devicehelper;

public abstract class SBIBioExceptionInfo {
	public abstract void initBioException(String[] bioException);

	public abstract void deInitBioException();

	protected SBIBioExceptionInfo() {
		super();
	}
}