package io.mosip.mock.sbi.service;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

import org.biometric.provider.CryptoUtility;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.devicehelper.face.SBIFaceCaptureInfo;
import io.mosip.mock.sbi.devicehelper.face.SBIFaceHelper;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapCaptureInfo;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapHelper;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleCaptureInfo;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleHelper;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.DatetimeHelper;
import io.mosip.mock.sbi.util.StringHelper;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.BioMetricsDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.DeviceDiscoveryRequestDetail;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceInfoDto;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.ErrorInfo;
import io.mosip.registration.mdm.dto.ProfileRequest;
import io.mosip.registration.mdm.dto.RCaptureResponse;
import io.mosip.registration.mdm.dto.StreamingRequestDetail;

public class SBIServiceResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(SBIServiceResponse.class);	
	
	protected int port = 0;
	protected String request = "";
	static Semaphore semaphore = new Semaphore (1);
    /**
     * Hashing Algorithm Used for encryption and decryption
     */
    private String algorithm = "SHA-256";

	public SBIServiceResponse (int port)
	{
		setPort (port);
	}
	public String getServiceresponse (SBIMockService mockService, Socket socket, String strJsonRequest)
    {
		String responseJson = "";
		setRequest (strJsonRequest);

		if (strJsonRequest.contains(SBIConstant.MOSIP_POST_VERB) ||
			strJsonRequest.contains(SBIConstant.MOSIP_GET_VERB) ||
			strJsonRequest.contains(SBIConstant.MOSIP_DISC_VERB))
		{
			responseJson = processDeviceDicoveryInfo (mockService);
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_INFO_VERB))
		{
			responseJson = processDeviceInfo (mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_STREAM_VERB))
		{
			responseJson = processLiveStreamInfo (mockService, socket);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_RCAPTURE_VERB))
		{
			responseJson = processRCaptureInfo (mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_SETPROFILE_VERB))
		{
			responseJson = processSetProfileInfo (mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_CAPTURE_VERB))
		{
			
		}
		else
		{
			responseJson = SBIResponseInfo.generateErrorResponse ("en", getPort (), "500", "");			
		}
		return responseJson;
    }
	
	public String processDeviceDicoveryInfo (SBIMockService mockService)
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();
        	 DeviceDiscoveryRequestDetail requestObject = (DeviceDiscoveryRequestDetail) getRequestJson (SBIConstant.MOSIP_DISC_VERB);
        	 String type = null;
        	 if (requestObject != null && requestObject.getType() != null && requestObject.getType().length() > 0)
        		 type = requestObject.getType().toString ().trim().toLowerCase();

             LOGGER.info("processDeviceDicoveryInfo :: type :: "+ type);

             List<DiscoverDto> infoList = new ArrayList<DiscoverDto> ();
             if (type == null || type.trim().length() == 0)
    		 {
            	 DiscoverDto discoverInfo = new DiscoverDto ();
            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
            		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
            		}};
            	 discoverInfo.error = errorMap;
            	 infoList.add(discoverInfo);
            	 return objectMapper.writeValueAsString(infoList);
    		 }
             else if (!type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS).trim().toLowerCase()))
             {
            	 DiscoverDto discoverInfo = new DiscoverDto ();
            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
            		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
            		}};
            	 discoverInfo.error = errorMap;
            	 infoList.add(discoverInfo);
            	 return objectMapper.writeValueAsString(infoList);
             }             
             else
             {
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
						 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER).trim().toLowerCase()))
			     {
				 	SBIFingerSlapHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
				 	deviceHelper.initDeviceDetails();
				 			
					DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
					if (discoverInfo != null)
						infoList.add(discoverInfo);
			     }
				 
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE).trim().toLowerCase()))
				 {
					 SBIFaceHelper deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
					 deviceHelper.initDeviceDetails();
					 DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
					 if (discoverInfo != null)
						 infoList.add(discoverInfo);
				 }
				
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS).trim().toLowerCase()))
				 {
					 SBIIrisDoubleHelper deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
					 deviceHelper.initDeviceDetails();
					 DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
					 if (discoverInfo != null)
						 infoList.add(discoverInfo);
				 }

				 if (infoList != null && infoList.size() > 0)
	        	 {
	            	 return objectMapper.writeValueAsString(infoList);
	        	 }
	             else
	             {
	            	 DiscoverDto discoverInfo = new DiscoverDto ();
	            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
	            		    put("503",  SBIJsonInfo.getErrorDescription(lang, "503"));
	            		}};
	            	 discoverInfo.error = errorMap;
	            	 infoList.add(discoverInfo);
	            	 return objectMapper.writeValueAsString(infoList);
	             }	 
             }
         }
         catch (Exception ex)
         {
             response = SBIResponseInfo.generateErrorResponse  (lang, getPort (), SBIConstant.Error_Code_999 + "", "");
             LOGGER.error("processDeviceDicoveryInfo", ex);
         }
         finally
         {
         }
         return response;
    }
	
	public String processDeviceInfo (SBIMockService mockService)
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();

             List<DeviceInfoDto> infoList = new ArrayList<DeviceInfoDto> ();
             SBIDeviceHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
             deviceHelper.initDeviceDetails();
             DeviceInfoDto deviceInfoDto = deviceHelper.getDeviceInfoDto();

             if (deviceInfoDto != null)
				infoList.add(deviceInfoDto);            	 

			 deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
			 deviceHelper.initDeviceDetails();
             deviceInfoDto = deviceHelper.getDeviceInfoDto();
             if (deviceInfoDto != null)
				infoList.add(deviceInfoDto);            	 

			 deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
			 deviceHelper.initDeviceDetails();
             deviceInfoDto = deviceHelper.getDeviceInfoDto();
             if (deviceInfoDto != null)
				infoList.add(deviceInfoDto);            	 

			 if (infoList != null && infoList.size() > 0)
        	 {
            	 return objectMapper.writeValueAsString(infoList);
        	 }
             else
             {
            	 deviceInfoDto = new DeviceInfoDto ();
            	 deviceInfoDto.setError(new ErrorInfo ("503", SBIJsonInfo.getErrorDescription(lang, "503"))); 
            	 infoList.add(deviceInfoDto);
            	 return objectMapper.writeValueAsString(infoList);
             }	 
         }
         catch (Exception ex)
         {
             response = SBIResponseInfo.generateErrorResponse  (lang, getPort (), SBIConstant.Error_Code_999 + "", "");
             LOGGER.error("processDeviceDicoveryInfo", ex);
         }
         finally
         {
         }
         return response;
    }

	public String processSetProfileInfo (SBIMockService mockService)
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();
        	 ProfileRequest requestObject = (ProfileRequest) getRequestJson (SBIConstant.MOSIP_SETPROFILE_VERB);
        	 String profileId = null;
        	 if (requestObject != null && requestObject.getProfileId() != null && requestObject.getProfileId().length() > 0)
        	 {
        		 mockService.setProfileId(profileId);
        		 ErrorInfo errorInfo = new ErrorInfo ("0", SBIJsonInfo.getErrorDescription(lang, "0")); 
        		 response = objectMapper.writeValueAsString(errorInfo);
        	 }	 
        	 else
        	 {
        		 mockService.setProfileId("Default");
        		 ErrorInfo errorInfo = new ErrorInfo ("0", SBIJsonInfo.getErrorDescription(lang, "0")); 
        		 response = objectMapper.writeValueAsString(errorInfo);
        	 }
         }
         catch (Exception ex)
         {
             response = SBIResponseInfo.generateErrorResponse  (lang, getPort (), "999", "");
             LOGGER.error("processSetProfileInfo", ex);
         }
         finally
         {
         }
         return response;
    }
	
	private String processLiveStreamInfo(SBIMockService mockService, Socket socket) {
		String response = null;
        String lang = "en";
        SBIDeviceHelper deviceHelper = null;
        try
        {
        	StreamingRequestDetail requestObject = (StreamingRequestDetail) getRequestJson (SBIConstant.MOSIP_STREAM_VERB);
            String deviceId = requestObject.getDeviceId();
            int deviceSubId = Integer.parseInt(requestObject.getDeviceSubId());

            LOGGER.info("processLiveStreamInfo :: deviceId :: "+ deviceId + " :: deviceSubId ::" + deviceSubId);

            if (deviceId != null && deviceId.trim().length() == 0)
            {
                return SBIJsonInfo.getErrorJson (lang, "604", "");
            }
            
            deviceHelper = getDeviceInfoForDeviceId (mockService, deviceId);
            if (deviceHelper.getDeviceInfo() == null)
            {
                return SBIJsonInfo.getErrorJson (lang, "605", "");
            }
            if (deviceHelper.getDeviceInfo() != null && !deviceHelper.getDeviceInfo().getPurpose().trim().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
            {
                return SBIJsonInfo.getErrorJson (lang, "606", "");
            }
            if (deviceHelper.getDeviceInfo() != null && !deviceHelper.getDeviceInfo().getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISREADY))
            {
                return SBIJsonInfo.getErrorJson (lang, "607", "");
            }
            
            deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISUSED);
            deviceHelper.initDevice();
            deviceHelper.setDeviceId(deviceId);
            deviceHelper.setDeviceSubId(deviceSubId);
            deviceHelper.getCaptureInfo().setLiveStreamStarted(true);
            renderMainHeaderData (socket);
            int returnCode = -1;
            while (true)
            {
            	if (deviceHelper.getCaptureInfo () == null)
                {
                    response = "ok";
                    break;
                }
            	
            	try
                {
            		// acquiring the lock 
            		if (semaphore != null)
            			semaphore.acquire(); 
            			
                    returnCode = deviceHelper.getLiveStream();                            

                    if (returnCode < 0)
                        break;
                    if (returnCode != 0)
                        continue;
                }
                catch (Exception ex)
                { }
            	finally
                {
                    try
                    {
                        if (semaphore != null)
                            semaphore.release();
                    }
                    catch (Exception ex)
                    { }
                }
            	
            	if (deviceHelper.getCaptureInfo() != null && deviceHelper.getCaptureInfo().getImage() != null)
                {
                    try
                    {
                        renderJPGImageData (socket, deviceHelper.getCaptureInfo().getImage());
                    }
                    catch (Exception ex)
                    {
                    	LOGGER.error ("processLiveStreamInfo :: Exception ::", ex);
                        break;
                    }
                }

                Thread.sleep (30);
            }
        	if (deviceHelper.getCaptureInfo() != null)
        	{
                deviceHelper.deInitDevice();
        		deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
        	}
            response = SBIJsonInfo.getErrorJson (lang, "608", "");
        }
        catch (Exception ex)
        {
            response = SBIJsonInfo.getErrorJson (lang, "610", ex.getLocalizedMessage());
            LOGGER.error("processLiveStreamInfo", ex);
        }
        finally
        {
        	try
            {
        		if (semaphore != null)
        			semaphore.release ();
            }
            catch (Exception ex)
            {
            }        	
        }
        return response;
	}

	private String processRCaptureInfo(SBIMockService mockService) {
		String response = null;
        String lang = "en";
        String specVersion = "";
        SBIDeviceHelper deviceHelper = null;
        try
        {
        	String deviceId = "";
        	int deviceSubId = 0;
        	CaptureRequestDto requestObject = (CaptureRequestDto) getRequestJson (SBIConstant.MOSIP_RCAPTURE_VERB);
        	List<CaptureRequestDeviceDetailDto> mosipBioRequest = null;
        	// if Null Throw Errors here
        	if (requestObject != null)
        	{
        		mosipBioRequest = requestObject.getBio();
        		if (mosipBioRequest != null && mosipBioRequest.size() > 0)
        		{
                    deviceId = requestObject.getBio().get(0).getDeviceId();
                    deviceSubId = Integer.parseInt(requestObject.getBio().get(0).getDeviceSubId());
        		}
        	}

            LOGGER.info("processRCaptureInfo :: deviceId :: "+ deviceId + " :: deviceSubId ::" + deviceSubId);

            if (deviceId != null && deviceId.trim().length() == 0)
            {
                return SBIJsonInfo.getErrorJson (lang, "704", "");
            }
            
            deviceHelper = getDeviceInfoForDeviceId (mockService, deviceId);
            if (deviceHelper.getDeviceInfo() == null)
            {
                return SBIJsonInfo.getErrorJson (lang, "705", "");
            }
            if (deviceHelper.getDeviceInfo() != null && !deviceHelper.getDeviceInfo().getPurpose().trim().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
            {
                return SBIJsonInfo.getErrorJson (lang, "706", "");
            }
            if (deviceHelper.getCaptureInfo() != null && 
            		(!deviceHelper.getDeviceId().trim().equalsIgnoreCase(deviceId) && 
    				 deviceHelper.getDeviceSubId() != deviceSubId))
            {
                return SBIJsonInfo.getErrorJson (lang, "702", "");
            }

            if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISREADY))
            {
                deviceHelper.initDevice();
                deviceHelper.setDeviceId(deviceId);
                deviceHelper.setDeviceSubId(deviceSubId);
                deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISUSED);
            }
            else if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISUSED))
            {
            	if (deviceHelper.getCaptureInfo().isCaptureStarted())
                    return SBIJsonInfo.getErrorJson (lang, "703", "");
            }
            
            String bioType = mosipBioRequest.get(0).getType();
            String [] bioException = mosipBioRequest.get(0).getException();// Bio exceptions
            int timeout = Integer.parseInt(requestObject.getTimeout()+ "");
            int requestScore = Integer.parseInt(mosipBioRequest.get(0).getRequestedScore() + "");
            
            specVersion = requestObject.getSpecVersion();
            int returnCode = -1;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeout;
            boolean captureStarted = false;    
            boolean captureTimeOut = false;
            boolean captureLiveStreamEnded = false;
            while (true)
            {
            	if (!captureStarted)
            	{
            		deviceHelper.setProfileId(mockService.getProfileId());
            		
            		if (!bioType.equals(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)))
            			deviceHelper.getCaptureInfo().getBioExceptionInfo().initBioException(bioException);

            		deviceHelper.getCaptureInfo().setRequestScore(requestScore);
                    deviceHelper.getCaptureInfo().setCaptureStarted(true);
            		captureStarted = true;
            	}
            	try
                {
            		if (System.currentTimeMillis () > endTime)
                    {
            			captureTimeOut = true;
                        break;
                    }
            		// acquiring the lock 
            		if (semaphore != null)
            			semaphore.acquire(); 
            			
            		if (deviceHelper.getCaptureInfo() == null)
                    {
                        captureLiveStreamEnded = true;
                        break;
                    }
            		
                    returnCode = deviceHelper.getBioCapture(false);

                    if (deviceHelper.getCaptureInfo() != null && deviceHelper.getCaptureInfo().isCaptureCompleted())
                    {
                        break;
                    }
                }
                catch (Exception ex)
                { }
            	finally
                {
                    try
                    {
                        if (semaphore != null)
                            semaphore.release();
                    }
                    catch (Exception ex)
                    { }
                }
            	
                Thread.sleep (30);
            }
            
            if (captureLiveStreamEnded)
            {
                response = SBIJsonInfo.getCaptureErrorJson  (specVersion, lang, "700", "", true);
            }
            else if (captureTimeOut)
            {
                response = SBIJsonInfo.getCaptureErrorJson  (specVersion, lang, "701", "", true);
        		if (deviceHelper.getCaptureInfo() == null)
        			deviceHelper.getCaptureInfo().setCaptureCompleted(true);
            }
            else
            {            	
            	List<BioMetricsDto> biometrics = getBioMetricsDtoList (lang, requestObject, deviceHelper, deviceSubId);
            	if (biometrics != null && biometrics.size() > 0)
            	{
                	RCaptureResponse captureResponse = new RCaptureResponse ();
	            	captureResponse.setBiometrics(biometrics);

	            	ObjectMapper mapper = new ObjectMapper ();	
	                SerializationConfig config = mapper.getSerializationConfig();
	                config.setSerializationInclusion(Inclusion.NON_NULL);
	                mapper.setSerializationConfig(config);

	                response = mapper.writeValueAsString(captureResponse);
        		}
            	else
            	{
                    response = SBIJsonInfo.getCaptureErrorJson  (specVersion, lang, "708", "", true);
            	}
 
                deviceHelper.deInitDevice();
        		deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
            }
            
        	if (deviceHelper.getCaptureInfo() != null)
        	{
                deviceHelper.getCaptureInfo().getBioExceptionInfo().deInitBioException();
                // When Capture is called After LiveStreaming is called
                // DeInit is called in Livestream method
                if (deviceHelper.getCaptureInfo().isLiveStreamStarted())
                {
                    deviceHelper.getCaptureInfo().setCaptureStarted(false);
                    deviceHelper.getCaptureInfo().setCaptureCompleted(true);               	
                }
                // DeInit When Capture is called Directly
                else
                {
                    deviceHelper.deInitDevice();
            		deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
                }
        	}
        }
        catch (Exception ex)
        {
            response = SBIJsonInfo.getCaptureErrorJson (specVersion, lang, "710", "", true);
            LOGGER.error("processRCaptureInfo", ex);
        }
        finally
        {
        	try
            {
        		if (semaphore != null)
        			semaphore.release ();
            }
            catch (Exception ex)
            {
            }                    
        }
        return response;
	}

	private List<BioMetricsDto> getBioMetricsDtoList (String lang, CaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, int deviceSubId) throws JsonGenerationException, JsonMappingException, IOException
	{
        List<BioMetricsDto> biometrics = new ArrayList<BioMetricsDto> ();
    	String specVersion = requestObject.getSpecVersion();
    	String transactionId = requestObject.getTransactionId();
    	int captureScore = 40; // SET MANUALLY
    	int requestScore = requestObject.getBio().get(0).getRequestedScore();
    	String bioType = requestObject.getBio().get(0).getType();
        String [] bioException = requestObject.getBio().get(0).getException();// Bio exceptions

    	String previousHash = requestObject.getBio().get(0).getPreviousHash() + "".trim ();  
    	// For Finger Slap
    	if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
    			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
    	{
    		SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo)deviceHelper.getCaptureInfo();
    		SBIFingerSlapBioExceptionInfo bioExceptionInfo = (SBIFingerSlapBioExceptionInfo)deviceHelper.getCaptureInfo().getBioExceptionInfo();
    		if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
    		{
    			if (bioExceptionInfo.getChkMissingLeftIndex() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_INDEX,
                                captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingLeftMiddle() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLM() != null && captureInfo.getBioValueLM().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_MIDDLE,
                                captureInfo.getBioValueLM(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingLeftRing() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLR() != null && captureInfo.getBioValueLR().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_RING,
                                captureInfo.getBioValueLR(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingLeftLittle() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLL() != null && captureInfo.getBioValueLL().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_LITTLE,
                                captureInfo.getBioValueLL(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    		}
    		else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
    		{
    			if (bioExceptionInfo.getChkMissingRightIndex() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_INDEX,
                                captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingRightMiddle() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRM() != null && captureInfo.getBioValueRM().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_MIDDLE,
                                captureInfo.getBioValueRM(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingRightRing() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRR() != null && captureInfo.getBioValueRR().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_RING,
                                captureInfo.getBioValueRR(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingRightLittle() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRL() != null && captureInfo.getBioValueRL().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_LITTLE,
                                captureInfo.getBioValueRL(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    		}
			else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
			{
    			if (bioExceptionInfo.getChkMissingLeftThumb() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLT() != null && captureInfo.getBioValueLT().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_THUMB,
                                captureInfo.getBioValueLT(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingRightThumb() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRT() != null && captureInfo.getBioValueRT().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_THUMB,
                                captureInfo.getBioValueRT(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
			}
    	}
    	// For IRIS DOUBLE
    	else if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
    			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
    	{
    		SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo)deviceHelper.getCaptureInfo();
    		SBIIrisDoubleBioExceptionInfo bioExceptionInfo = (SBIIrisDoubleBioExceptionInfo)deviceHelper.getCaptureInfo().getBioExceptionInfo();

    		if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
			{
    			if (bioExceptionInfo.getChkMissingLeftIris() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS,
                                captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
			}
			else if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
			{
    			if (bioExceptionInfo.getChkMissingRightIris() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS,
                                captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
			}
			else if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_BOTH)
			{
    			if (bioExceptionInfo.getChkMissingLeftIris() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS,
                                captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
    			if (bioExceptionInfo.getChkMissingRightIris() == SBICheckState.Unchecked)
    			{
    				if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS,
                                captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", false);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
			}    		
    	}    
    	// For Face
    	else if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
    			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
    	{
    		SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo)deviceHelper.getCaptureInfo();
    		
    		boolean isExceptionPhoto = false;    		
    		if (bioException != null && bioException.length > 0)
    			isExceptionPhoto = true;
            LOGGER.info("getBioMetricsDtoList :: isExceptionPhoto :: " + isExceptionPhoto);

    		if (!isExceptionPhoto)
			{
				if (captureInfo.getBioValueFace() != null && captureInfo.getBioValueFace().length() > 0)
				{
    				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
                            captureInfo.getBioValueFace(), captureScore, requestScore, "", "100", false);
                    if (bioDto != null)
                    {
                    	biometrics.add(bioDto);
                        previousHash = bioDto.getHash();
                    }
				}
				else
				{
					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                    if (bioDto != null)
                    {
                    	biometrics.add (bioDto);
                        previousHash = bioDto.getHash();
                    }
                }
			}
    		else
			{
                LOGGER.info("getBioMetricsDtoList :: 1111 :: ");
				if (captureInfo.getBioValueExceptionPhoto() != null && captureInfo.getBioValueExceptionPhoto().length() > 0)
				{
    				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
                            captureInfo.getBioValueExceptionPhoto(), captureScore, requestScore, "", "100", false);
                    if (bioDto != null)
                    {
                    	biometrics.add(bioDto);
                        previousHash = bioDto.getHash();
                    }
                    LOGGER.info("getBioMetricsDtoList :: previousHash :: " + previousHash);
				}
				else
				{
					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, true);
                    if (bioDto != null)
                    {
                    	biometrics.add (bioDto);
                        previousHash = bioDto.getHash();
                    }
                }
			}
    	}
    	return biometrics;
	}
	
	private BioMetricsDto getBiometricData (String txnID, CaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, 
			String previousHash, String bioType, String bioSubType, String bioValue, 
			int qualityScore, int qualityRequestScore, String lang, String errorCode, boolean isUsedForAuthenication) throws JsonGenerationException, JsonMappingException, IOException
    {
		DeviceInfo deviceInfo = deviceHelper.getDeviceInfo();
		
		BioMetricsDto biometric = new BioMetricsDto ();
        biometric.setSpecVersion(requestObject.getSpecVersion());

        biometric.setError(new ErrorInfo (errorCode, SBIJsonInfo.getErrorDescription (lang, errorCode)));

        BioMetricsDataDto biometricData = new BioMetricsDataDto ();
        biometricData.setDeviceCode(deviceInfo.getDeviceCode());
        biometricData.setDigitalId(deviceInfo.getDigitalId());
        biometricData.setDeviceServiceVersion(deviceInfo.getServiceVersion());
        biometricData.setBioType(bioType);
        biometricData.setBioSubType(bioSubType);

        biometricData.setPurpose(requestObject.getPurpose());
        biometricData.setEnv(requestObject.getEnv());

        if (isUsedForAuthenication)
            biometricData.setDomainUri(requestObject.getDomainUri() + "");

        byte [] secretKey = null;
        String timeStamp = DatetimeHelper.getISO8601CurrentDate ();

        if (isUsedForAuthenication == false)
        {
            biometricData.setBioValue(bioValue);
        }

        biometricData.setTimestamp(timeStamp);
        biometricData.setRequestedScore(qualityRequestScore + "");
        biometricData.setQualityScore(10 + "");
        biometricData.setTransactionId(txnID);

        ObjectMapper mapper = new ObjectMapper ();	
        SerializationConfig config = mapper.getSerializationConfig();
        config.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationConfig(config);

		String currentBioData = mapper.writeValueAsString(biometricData);

        //base64 signature of the data block. base64 signature of the hash element
        String dataBlockSignBase64 = deviceHelper.getSignBioMetricsDataDto (deviceHelper.getDeviceType(), deviceHelper.getDeviceSubType(), currentBioData);
        //GetLogger ().Info ("currentBioData>dataBlockSignBase64>base64>" + dataBlockSignBase64);

        //GetLogger ().Info ("currentBioData>dataBlockVerify>>" + cryptoService.VerifyBioCaptureUsingJWS (currentBioData, dataBlockSignBase64, deviceInfo.SerialNo));

        biometric.setData (dataBlockSignBase64);
        //GetLogger ().Info ("currentBioData>Base64>>" + biometric.data);

        String previousDataHashSHA256Hex = "";
        if (previousHash == null || previousHash.trim().length() == 0)
        {
            byte [] previousDataByteArr = StringHelper.toUtf8ByteArray (previousHash);
            previousDataHashSHA256Hex = StringHelper.toHexaFromByteArray (CryptoUtility.generateHash(previousDataByteArr, algorithm));
        }
        else
        {
            previousDataHashSHA256Hex = previousHash;
        }

        byte [] currentDataByteArr = StringHelper.toUtf8ByteArray (currentBioData);
        String currentDataHashSHA256Hex = StringHelper.toHexaFromByteArray (CryptoUtility.generateHash (currentDataByteArr, algorithm));

        String finalDataHash = previousDataHashSHA256Hex + currentDataHashSHA256Hex;
        byte [] finalDataByteArr = StringHelper.toUtf8ByteArray (finalDataHash);
        biometric.setHash(StringHelper.toHexaFromByteArray (CryptoUtility.generateHash (finalDataByteArr, algorithm)));

        return biometric;
    }

	private BioMetricsDto getBiometricErrorData (String lang, String specVersion, boolean isRCapture)
	{
	    BioMetricsDto biometric = new BioMetricsDto ();
	    biometric.setSpecVersion(specVersion);
	    biometric.setData("");
	    biometric.setHash("");
	    if (isRCapture == false)
	    {
	        biometric.setSessionKey("");
	        biometric.setThumbprint("");
	    }

	    biometric.setError(new ErrorInfo ("701", (SBIJsonInfo.getErrorDescription (lang, "701")).trim()));
	    
	    return biometric;
	}

	private void renderMainHeaderData (Socket socket) throws IOException
	{
		writeMainHeader (socket);
	}
	
	private void writeMainHeader (Socket socket) throws IOException
	{
		// prepare main header
		byte [] mainHeader = createMainHeader ();

        BufferedOutputStream outputStream = new BufferedOutputStream (socket.getOutputStream());
        outputStream.write (mainHeader, 0, mainHeader.length);
        outputStream.flush ();
        outputStream.flush ();
	}
	
	 private byte [] createMainHeader ()
     {
         String header =
             "HTTP/1.0 200 OK\r\n" +
             "Server: http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort () + "\r\n" +
             "Connection: close\r\n" +
             "Max-Age: 0\r\n" +
             "Expires: 0\r\n" +
             "Cache-Control: no-cache, private\r\n" +
             "Pragma: no-cache\r\n" +
             "Content-Type: multipart/x-mixed-replace; " +
             "boundary=--BoundaryString\r\n\r\n";

         // using ascii encoder is fine since there is no international character used in this string.
         return header.getBytes(StandardCharsets.US_ASCII);
     }
	 
	private void renderJPGImageData (Socket socket, byte[] image) throws IOException
    {
        if (image != null && socket != null && !socket.isClosed())
            writeFrame (socket, image);
    }
	
	private void writeFrame (Socket socket, byte[] image) throws IOException
    {
        // prepare image data
        byte[] imageInByte = image;
        
        // prepare header
        byte [] header = createHeader (imageInByte.length);
        // prepare footer
        byte [] footer = createFooter ();

        BufferedOutputStream outputStream = new BufferedOutputStream (socket.getOutputStream());
        // Start writing data
        outputStream.write(header, 0, header.length);
        outputStream.write(imageInByte, 0, imageInByte.length);
        outputStream.write(footer, 0, footer.length);
        outputStream.flush();
        outputStream.flush();
   	}

	private byte [] createHeader (int length)
	{
         String header =
             "--BoundaryString\r\n" +
             "Content-Type:image/jpeg\r\n" +
             "Content-Length:" + length + "\r\n\r\n"; // there are always 2 new line character before the actual data

         // using ascii encoder is fine since there is no international character used in this string.
         return header.getBytes(StandardCharsets.US_ASCII);
	}
	 
	public byte [] createFooter ()
	{
		 return "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
	}
	 
	private SBIDeviceHelper getDeviceInfoForDeviceId(SBIMockService mockService, String deviceId) {
		
        SBIDeviceHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
        deviceHelper.initDeviceDetails();
        if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().trim().equals(deviceId.trim()))
        {
        	return deviceHelper; 
        }
        
        deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
        deviceHelper.initDeviceDetails();
        if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().trim().equals(deviceId.trim()))
        {
        	return deviceHelper; 
        }

        deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
        deviceHelper.initDeviceDetails();
        if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().trim().equals(deviceId.trim()))
        {
        	return deviceHelper; 
        }

        return null;
	}

	public Object getRequestJson (String methodVerb)
    {
        if (getRequest () != null && getRequest().indexOf("{") >= 0)
        {
            try
            {
            	ObjectMapper mapper = new ObjectMapper();
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_DISC_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), DeviceDiscoveryRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_STREAM_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), StreamingRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_RCAPTURE_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), CaptureRequestDto.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_SETPROFILE_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), ProfileRequest.class);
        		
        		return null;
            }
            catch (Exception ex)
            {
            	LOGGER.error("getRequestJson", ex);
                return null;
            }
        }
        else
        {
            return null;
        }
    }
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}	
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public static Semaphore getSemaphore() {
		return semaphore;
	}
	public static void setSemaphore(Semaphore semaphore) {
		SBIServiceResponse.semaphore = semaphore;
	}		 
}
