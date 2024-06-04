package io.mosip.mock.sbi.devicehelper.finger.single;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIFingerSingleHelper extends SBIDeviceHelper {
	private SBIFingerSingleHelper(int port, String purpose, String keystoreFilePath, String biometricImageType) {
		super(port, purpose, SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE,
				keystoreFilePath, biometricImageType);
	}

	// synchronized method to control simultaneous access
	public static synchronized SBIFingerSingleHelper getInstance(int port, String purpose, String keystoreFilePath,
			String biometricImageType) {
		return new SBIFingerSingleHelper(port, purpose, keystoreFilePath, biometricImageType);
	}

	@Override
	public long initDevice() {
		SBIFingerSingleCaptureInfo captureInfo = new SBIFingerSingleCaptureInfo();
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
	@SuppressWarnings({ "java:S3776" })
	public int getBioCapture(boolean isUsedForAuthenication) throws Exception {
		byte[] isoData = null;

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

		if (isUsedForAuthenication && getDeviceSubId() == SBIConstant.DEVICE_FINGER_SINGLE_SUB_TYPE_ID) {
			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLI()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_INDEX,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreLI(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureLI(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLM()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_MIDDLE,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreLM(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureLM(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLR()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_RING,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreLR(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureLR(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLL()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_LITTLE,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreLL(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureLL(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRI()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_INDEX,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreRI(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureRI(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRM()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_MIDDLE,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreRM(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureRM(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRR()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_RING,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreRR(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureRR(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRL()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_LITTLE,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreRL(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureRL(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLT()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_THUMB,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreLT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreLT(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureLT(true);
			}

			isoData = getBiometricImageType().equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
					? getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB_WSQ)
					: getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB);
			if (isoData != null && !((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRT()) {
				getCaptureInfo().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_THUMB,
						StringHelper.base64UrlEncode(isoData));
				if (isScoreFromIso())
					((SBIFingerSingleCaptureInfo) getCaptureInfo())
							.setCaptureScoreRT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
				else
					((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureScoreRT(getQualityScore());
				((SBIFingerSingleCaptureInfo) getCaptureInfo()).setCaptureRT(true);
			}

			if (((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLI()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLM()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLR()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLL()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRI()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRM()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRR()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRL()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureRT()
					|| ((SBIFingerSingleCaptureInfo) getCaptureInfo()).isCaptureLT()) {
				getCaptureInfo().setCaptureCompleted(true);
			}
		}
		return 0;
	}
}