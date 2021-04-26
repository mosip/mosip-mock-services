package io.mosip.mock.sbi.devicehelper.iris.binacular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIIrisDoubleHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIIrisDoubleHelper.class);	

	private static SBIIrisDoubleHelper instance; 
	  
	private SBIIrisDoubleHelper(int port, String purpose, String biometricType)  
	{ 
		super (port, purpose, ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS), ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE), biometricType);
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIIrisDoubleHelper getInstance(int port, String purpose, String biometricType)  
	{ 
		if (instance == null)  
		{ 
			// if instance is null, initialize 
			instance = new SBIIrisDoubleHelper(port, purpose, biometricType); 
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
	public int getBioCapture(boolean isForAuthenication) {
		byte [] isoImage = null;
		
		if (!isForAuthenication)
		{
			if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
			{
				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIris() == SBICheckState.Unchecked)
				{
					isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
					if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoImage));
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
					isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
					if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoImage));
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
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
			{
				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIris() == SBICheckState.Unchecked)
				{
					isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
					if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoImage));
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
					}				
				}
				else
				{
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}			

				if (((SBIIrisDoubleBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightIris() == SBICheckState.Unchecked)
				{
					isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
					if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					{
						((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoImage));
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
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
				if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS, StringHelper.base64UrlEncode(isoImage));
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				
				
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
					getCaptureInfo ().setCaptureCompleted(true);
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
				if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS, StringHelper.base64UrlEncode(isoImage));
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				
				
				if (((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
					getCaptureInfo ().setCaptureCompleted(true);
			}
			else if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_IRIS);
				if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS, StringHelper.base64UrlEncode(isoImage));
					((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
				}				

				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_IRIS);
				if (isoImage != null && !((SBIIrisDoubleCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS, StringHelper.base64UrlEncode(isoImage));
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
