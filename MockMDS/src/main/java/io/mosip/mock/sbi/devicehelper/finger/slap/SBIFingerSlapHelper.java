package io.mosip.mock.sbi.devicehelper.finger.slap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICaptureInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIFingerSlapHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFingerSlapHelper.class);	

	private static SBIFingerSlapHelper instance; 
	  
	private SBIFingerSlapHelper(int port)  
	{ 
		super (port, ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER), ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIFingerSlapHelper getInstance(int port)  
	{ 
		if (instance == null)  
		{ 
			// if instance is null, initialize 
			instance = new SBIFingerSlapHelper(port); 
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
	public int getBioCapture(boolean isUsedForAuthenication) {
		byte [] isoImage = null;
		if (getDeviceSubId () == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
		{
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftIndex() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_INDEX);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLI())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLI(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLI(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLI(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftMiddle() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_MIDDLE);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLM())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLM(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLM(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLM(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftRing() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_RING);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLR())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLR(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLR(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLR(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingLeftLittle() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_LITTLE);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLL())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLL(StringHelper.base64UrlEncode(isoImage));
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
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_INDEX);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRI())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRI(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRI(true);
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightMiddle() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_MIDDLE);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRM())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRM(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRM(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRM(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightRing() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_RING);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRR())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRR(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRR(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRR(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightLittle() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_LITTLE);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRL())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRL(StringHelper.base64UrlEncode(isoImage));
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
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_LEFT_THUMB);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureLT())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueLT(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLT(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureLT(true);				
			}
			
			if (((SBIFingerSlapBioExceptionInfo)getCaptureInfo ().getBioExceptionInfo()).getChkMissingRightThumb() == SBICheckState.Unchecked)
			{
				isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_RIGHT_THUMB);
				if (isoImage != null && !((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
				{
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setBioValueRT(StringHelper.base64UrlEncode(isoImage));
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRT(true);
				}				
			}
			else
			{
				((SBIFingerSlapCaptureInfo)getCaptureInfo ()).setCaptureRT(true);				
			}
			
			if (((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT() ||
					((SBIFingerSlapCaptureInfo)getCaptureInfo ()).isCaptureRT())
			{
				getCaptureInfo ().setCaptureCompleted(true);
			}
		}
		// TODO Auto-generated method stub
		return 0;
	} 	
}
