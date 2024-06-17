package io.mosip.mock.sbi.devicehelper.finger.slap;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIFingerSlapHelper extends SBIDeviceHelper {
	private SBIFingerSlapHelper(int port, String purpose, String keystorePath, String biometricImageType) {
		super(port, purpose, SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP,
				keystorePath, biometricImageType);
	}

	// synchronized method to control simultaneous access
	public static synchronized SBIFingerSlapHelper getInstance(int port, String purpose, String keystorePath,
			String biometricImageType) {
		return new SBIFingerSlapHelper(port, purpose, keystorePath, biometricImageType);
	}

	@Override
	public long initDevice() {
		SBIFingerSlapCaptureInfo captureInfo = new SBIFingerSlapCaptureInfo();
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
					.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_FINGER) != null) {
				seedValue = Integer.parseInt(
						ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_FINGER));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			} else if (this.getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION) && ApplicationPropertyHelper
					.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_FINGER) != null) {
				seedValue = Integer.parseInt(ApplicationPropertyHelper
						.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_FINGER));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			}
		}

		switch (getDeviceSubId()) {
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
			setBioCaptureFingerprintForSubTypeLeft(isUsedForAuthenication, seedName);
			break;
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
			setBioCaptureFingerprintForSubTypeRight(isUsedForAuthenication, seedName);
			break;
		case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
			setBioCaptureFingerprintForSubTypeThumb(isUsedForAuthenication, seedName);
			break;
		default:
			break;
		}

		return 0;
	}

	@SuppressWarnings({ "java:S3776" })
	private void setBioCaptureFingerprintForSubTypeThumb(boolean isUsedForAuthenication, String seedName)
			throws Exception {
		byte[] isoData = null;
		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftThumb() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLT()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueLT(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_THUMB,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreLT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreLT(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLT(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLT(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightThumb() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRT()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueRT(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_THUMB,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreRT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreRT(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRT(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRT(true);
		}

		if (((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLT()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRT()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}

	@SuppressWarnings({ "java:S3776" })
	private void setBioCaptureFingerprintForSubTypeRight(boolean isUsedForAuthenication, String seedName)
			throws Exception {
		byte[] isoData = null;
		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightIndex() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRI()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_INDEX,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreRI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreRI(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRI(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRI(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightMiddle() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRM()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueRM(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_MIDDLE,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreRM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreRM(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRM(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRM(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightRing() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRR()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueRR(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_RING,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreRR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreRR(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRR(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRR(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingRightLittle() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRL()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueRL(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_LITTLE,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreRL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreRL(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRL(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureRL(true);
		}

		if (((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRI()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRM()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRR()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureRL()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}

	@SuppressWarnings({ "java:S3776" })
	private void setBioCaptureFingerprintForSubTypeLeft(boolean isUsedForAuthenication, String seedName)
			throws Exception {
		byte[] isoData = null;
		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftIndex() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLI()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_INDEX,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreLI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreLI(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLI(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLI(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftMiddle() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLM()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueLM(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_MIDDLE,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreLM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreLM(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLM(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLM(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftRing() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLR()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueLR(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_RING,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreLR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreLR(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLR(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLR(true);
		}

		if (((SBIFingerSlapBioExceptionInfo) getCaptureInfo().getBioExceptionInfo())
				.getChkMissingLeftLittle() == SBICheckState.UNCHECKED) {
			isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE);
			if (isoData != null && !((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLL()) {
				if (!isUsedForAuthenication) {
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setBioValueLL(StringHelper.base64UrlEncode(isoData));
				} else {
					getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_LITTLE,
							StringHelper.base64UrlEncode(isoData));
				}
				if (isScoreFromIso())
					((SBIFingerSlapCaptureInfo) getCaptureInfo())
							.setCaptureScoreLL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureScoreLL(getQualityScore());
				((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLL(true);
			}
		} else {
			((SBIFingerSlapCaptureInfo) getCaptureInfo()).setCaptureLL(true);
		}

		if (((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLI()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLM()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLR()
				|| ((SBIFingerSlapCaptureInfo) getCaptureInfo()).isCaptureLL()) {
			getCaptureInfo().setCaptureCompleted(true);
		}
	}
}