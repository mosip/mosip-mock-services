package io.mosip.mock.sbi.devicehelper.finger.slap;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIFingerSlapHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFingerSlapHelper.class);	

	private static SBIFingerSlapHelper instance; 
	  
	private SBIFingerSlapHelper(int port, String purpose)  
	{ 
		super (port, purpose, ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER), ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIFingerSlapHelper getInstance(int port, String purpose)  
	{ 
		if (instance == null)  
		{ 
			// if instance is null, initialize 
			instance = new SBIFingerSlapHelper(port, purpose); 
		} 
		return instance; 
	}

	@Override
	public long initDevice() {
		SBIFingerSlapCaptureInfo captureInfo = new SBIFingerSlapCaptureInfo ();
		captureInfo.initCaptureInfo();
		setCaptureInfo (captureInfo);
		return 0;
	}

	@Override
	public int deInitDevice() {
		if (getCaptureInfo () != null)
			getCaptureInfo ().deInitCaptureInfo ();
		setCaptureInfo (null);
		return 0;
	}

	@Override
	public int getLiveStream() {
		byte [] image = getLiveStreamBufferedImage();
		if (image == null || image.length == 0)
			return -1;
		getCaptureInfo ().setImage(image);
		
		return 0;
	}

	@Override
	public int getBioCapture(boolean isUsedForAuthenication) throws Exception {
		byte [] isoData = null;

		String seedName = "";
		if (this.getProfileId().equalsIgnoreCase(SBIConstant.PROFILE_AUTOMATIC))
		{
			if (ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SEED_FINGER) != null)
			{
				int seedValue = Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SEED_FINGER));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			}
		}

		if (!isUsedForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
			{
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIndex() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLI())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftMiddle() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLM())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLM(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLM(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLM(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLM(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftRing() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLR())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLR(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLR(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLR(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLR(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftLittle() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLL())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLL(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLL(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLL(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLL(true);				
				}

				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLI() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLM() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLR() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLL())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
			{
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightIndex() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRI())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightMiddle() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRM())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRM(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRM(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRM(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRM(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightRing() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRR())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRR(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRR(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRR(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRR(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightLittle() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRL())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRL(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRL(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRL(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRL(true);				
				}

				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRI() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRM() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRR() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRL())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
			{
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftThumb() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLT())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLT(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLT(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLT(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLT(true);				
				}
				
				if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightThumb() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB);
					if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
					{
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRT(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRT(getQualityScore());
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRT(true);
					}				
				}
				else
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRT(true);				
				}
				
				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLT() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
		}
		else if (isUsedForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_INDEX, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLM())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_MIDDLE, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLM(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLM(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLR())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_RING, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLR(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLR(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLL())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_LITTLE, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLL(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLL(true);
				}				

				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLI() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLM() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLR() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLL())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_INDEX, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRM())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_MIDDLE, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRM(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRM(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRM(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRR())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_RING, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRR(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRR(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRR(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRL())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_LITTLE, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRL(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRL(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRL(true);
				}				

				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRI() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRM() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRR() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRL())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLT())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_THUMB, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreLT(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLT(true);
				}				
				
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB);
				if (isoData != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_THUMB, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRT(BioUtilHelper.getFingerQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureScoreRT(getQualityScore());
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRT(true);				
				}				
				
				if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT() ||
						((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
		}
		return 0;
	} 	
}
