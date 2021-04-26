package io.mosip.mock.sbi.devicehelper.face;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.StringHelper;

public class SBIFaceHelper extends SBIDeviceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIFaceHelper.class);	

	private static SBIFaceHelper instance; 
	  
	private SBIFaceHelper(int port, String purpose, String biometricType)  
	{ 
		super (port, purpose, ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE), ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE), biometricType);
	} 
  
	//synchronized method to control simultaneous access 
	synchronized public static SBIFaceHelper getInstance(int port, String purpose, String biometricType)  
	{ 
		if (instance == null)  
		{ 
			// if instance is null, initialize 
			instance = new SBIFaceHelper(port, purpose, biometricType); 
		} 
		return instance; 
	}

	@Override
	public long initDevice() {
		SBIFaceCaptureInfo captureInfo = new SBIFaceCaptureInfo ();
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

		if (!isUsedForAuthenication)
		{
			isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_FACE);
			if (isoImage != null && !((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureFace())
			{
				((SBIFaceCaptureInfo)getCaptureInfo ()).setBioValueFace(StringHelper.base64UrlEncode(isoImage));
			}				
			((SBIFaceCaptureInfo)getCaptureInfo ()).setCaptureFace(true);				
			
			isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_FACE_EXCEPTION);
			if (isoImage != null && !((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureExceptionPhoto())
			{
				((SBIFaceCaptureInfo)getCaptureInfo ()).setBioValueExceptionPhoto(StringHelper.base64UrlEncode(isoImage));
			}				
			((SBIFaceCaptureInfo)getCaptureInfo ()).setCaptureExceptionPhoto(true);				

			if (((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureFace() ||
					((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureExceptionPhoto())
			{
				getCaptureInfo ().setCaptureCompleted(true);
			}
		}
		else
		{
			isoImage = getBiometricISOImage(SBIConstant.PROFILE_BIO_FILE_NAME_FACE);
			if (isoImage != null && !((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureFace())
			{
				getCaptureInfo ().addBiometricForBioSubType(SBIConstant.BIO_NAME_UNKNOWN, StringHelper.base64UrlEncode(isoImage));
				((SBIFaceCaptureInfo)getCaptureInfo ()).setCaptureFace(true);				
			}				
			if (((SBIFaceCaptureInfo)getCaptureInfo ()).isCaptureFace())
				getCaptureInfo ().setCaptureCompleted(true);
		}
		return 0;
	} 	
}
