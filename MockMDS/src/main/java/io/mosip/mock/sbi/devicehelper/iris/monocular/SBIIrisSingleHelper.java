package io.mosip.mock.sbi.devicehelper.iris.monocular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIIrisSingleHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIIrisSingleHelper.class);	

	private SBIIrisSingleHelper(int port, String purpose, String keystoreFilePath, String biometricImageType)
	{ 
		super (port, purpose, SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS, SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE,
				keystoreFilePath, biometricImageType);
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIIrisSingleHelper getInstance(int port, String purpose, String keystoreFilePath, String biometricImageType)
	{ 
		return new SBIIrisSingleHelper(port, purpose, keystoreFilePath, biometricImageType);
	}

	@Override
	public long initDevice() {
		SBIIrisSingleCaptureInfo captureInfo = new SBIIrisSingleCaptureInfo ();
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
			int seedValue = -1;
			if (this.getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH))
			{
				if (ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_IRIS) != null)
				{
					seedValue = Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_AUTH_SEED_IRIS));
					seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
				}
			}
			else if (this.getPurpose().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
			{
				if (ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS) != null)
				{
					seedValue = Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS));
					seedName = String.format("%04d", getRandomNumberForSeed(seedValue)).trim();
				}
			}
		}

		if (isForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SINGLE_SUB_TYPE_ID)
			{
				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
				if (isoData != null && !((SBIIrisSingleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureScoreLI(getQualityScore());
					((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				

				isoData = getBiometricISOImage(seedName, SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
				if (isoData != null && !((SBIIrisSingleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS, StringHelper.base64UrlEncode(isoData));
					if (isScoreFromIso())
						((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(BioUtilHelper.getIrisQualityScoreFromIso(getPurpose(), isoData));
					else
						((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureScoreRI(getQualityScore());
					((SBIIrisSingleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				

				if (((SBIIrisSingleCaptureInfo)getCaptureInfo ()).isCaptureLI() ||
						((SBIIrisSingleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().setCaptureCompleted(true);
				}
			}
		}
		return 0;
	} 	
}
