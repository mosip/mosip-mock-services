package io.mosip.mock.sbi.devicehelper.iris.binacular;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIIrisDoubleHelper extends SBIDeviceHelper {
	private SBIIrisDoubleHelper(int port, String purpose, String keystoreFilePath, String biometricImageType) {
		super(port, purpose, SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS, SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE,
				keystoreFilePath, biometricImageType);
	}

	// synchronized method to control simultaneous access
	public static synchronized SBIIrisDoubleHelper getInstance(int port, String purpose, String keystoreFilePath,
			String biometricImageType) {
		return new SBIIrisDoubleHelper(port, purpose, keystoreFilePath, biometricImageType);
	}

	@Override
	public long initDevice() {
		SBIIrisDoubleCaptureInfo captureInfo = new SBIIrisDoubleCaptureInfo();
		captureInfo.initCaptureInfo();
		setCaptureInfo(captureInfo);
		return 0;
	}

	@Override
	public int deInitDevice() {
		if (getCaptureInfo() != null)
			getCaptureInfo().deInitCaptureInfo();
		setCaptureInfo(null);
		return 0;
	}

	@Override
	public int getLiveStream() {
		byte[] image = getLiveStreamBufferedImage();
		if (image == null || image.length == 0)
			return -1;
		getCaptureInfo().setImage(image);

		return 0;
	}

	@Override
	public int getBioCapture(boolean isUsedForAuthenication) throws Exception {
		String seedName = "";
		if (this.getProfileId().equalsIgnoreCase(SBIConstant.PROFILE_AUTOMATIC)) {
			int seedValue = -1;
			if (this.getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH) && ApplicationPropertyHelper
					.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_IRIS) != null) {
				seedValue = Integer.parseInt(
						ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_IRIS));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			} else if (this.getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION) && ApplicationPropertyHelper
					.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS) != null) {
				seedValue = Integer.parseInt(ApplicationPropertyHelper
						.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			}
		}

		switch (getDeviceSubId()) {
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
			setBioCaptureIrisForSubTypeLeft(isUsedForAuthenication, seedName);
			break;
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
			setBioCaptureIrisForSubTypeRight(isUsedForAuthenication, seedName);
			break;
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
			setBioCaptureIrisForSubTypeBoth(isUsedForAuthenication, seedName);
			break;
		default:
			break;
		}

		return 0;
	}

	private void setBioCaptureIrisForSubTypeLeft(boolean isUsedForAuthenication, String seedName) throws Exception {
		byte[] isoData = null;
		if (((SBIIrisDoubleBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftIris() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
			if (isoData != null && !((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureLI()) {
				if (!isUsedForAuthenication) {
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS,
							StringHelper.base64UrlEncode(isoData));
				}

				if (isScoreFromIso())
					((SBIIrisDoubleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureScoreLI(getQualityScore());
				((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
			}
		} else {
			((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
		}

		if (((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureLI()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}

	private void setBioCaptureIrisForSubTypeRight(boolean isUsedForAuthenication, String seedName) throws Exception {
		byte[] isoData = null;
		if (((SBIIrisDoubleBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightIris() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
			if (isoData != null && !((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureRI()) {
				if (!isUsedForAuthenication) {
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIIrisDoubleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureScoreRI(getQualityScore());
				((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureRI(true);
			}
		} else {
			((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
		}
		if (((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureRI()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}

	@SuppressWarnings({ "java:S3776" })
	private void setBioCaptureIrisForSubTypeBoth(boolean isUsedForAuthenication, String seedName) throws Exception {
		byte[] isoData = null;
		if (((SBIIrisDoubleBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftIris() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
			if (isoData != null && !((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureLI()) {
				if (!isUsedForAuthenication) {
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIIrisDoubleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureScoreLI(getQualityScore());
				((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
			}
		} else {
			((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
		}

		if (((SBIIrisDoubleBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightIris() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
			if (isoData != null && !((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureRI()) {
				if (!isUsedForAuthenication) {
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIIrisDoubleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureScoreRI(getQualityScore());
				((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureRI(true);
			}
		} else {
			((SBIIrisDoubleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
		}

		if (((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureLI()
				|| ((SBIIrisDoubleCaptureInfo) getCaptureInfo()).isCaptureRI()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}
}