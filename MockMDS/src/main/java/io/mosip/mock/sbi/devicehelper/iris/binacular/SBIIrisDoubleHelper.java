package io.mosip.mock.sbi.devicehelper.iris.binacular;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapCaptureInfo;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIIrisDoubleHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIIrisDoubleHelper.class);	

	private static SBIIrisDoubleHelper instance; 
	  
	private SBIIrisDoubleHelper(int port, String purpose)  
	{ 
		super (port, purpose, ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS), ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIIrisDoubleHelper getInstance(int port, String purpose)  
	{ 
		if (instance == null)  
		{ 
			// if instance is null, initialize 
			instance = new SBIIrisDoubleHelper(port, purpose); 
		} 
		return instance; 
	}

	@Override
	public long initDevice() {
		SBIIrisDoubleCaptureInfo captureInfo = new SBIIrisDoubleCaptureInfo ();
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
	public int getBioCapture(boolean isForAuthenication) throws Exception {
		byte [] isoData = null;
		
		String seedName = "";
		if (this.getProfileId().equalsIgnoreCase(SBIConstant.PROFILE_AUTOMATIC))
		{
			if (ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SEED_IRIS) != null)
			{
				int seedValue = Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SEED_IRIS));
				seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
			}
		}

		if (!isForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
			{
				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIris() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
					if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
					}				
				}
				else
				{
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}	
				
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
			{
				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightIris() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
					if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
					}				
				}
				else
				{
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}			
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_BOTH)
			{
				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIris() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
					if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
					}				
				}
				else
				{
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}			

				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightIris() == SBICheckState.Unchecked)
				{
					isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
					if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoData));
						if (isScoreFromIso())
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
						else
							((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
					}				
				}
				else
				{
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}			

				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI() ||
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
		}
		else if (isForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
				if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				
				
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					getCaptureInfo ().setCaptureCompleted(true);
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
				if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureRI(true);				
				}				
				
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					getCaptureInfo ().setCaptureCompleted(true);
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_BOTH)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
				if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				

				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
				if (isoData != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				

				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI() ||
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
		}
		return 0;
	} 	
}
