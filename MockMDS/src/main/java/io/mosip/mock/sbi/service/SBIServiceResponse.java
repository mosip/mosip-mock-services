package io.mosip.mock.sbi.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.biometric.provider.CryptoUtility;
import org.biometric.provider.JwtUtility;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioSubTypeInfo;
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
import io.mosip.mock.sbi.util.StringHelper;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.BioMetricsDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailSbiDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.DeviceDiscoveryRequestDetail;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceInfoDto;
import io.mosip.registration.mdm.dto.DeviceSBIInfo;
import io.mosip.registration.mdm.dto.DeviceSbiInfoDto;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.DiscoverSBIDto;
import io.mosip.registration.mdm.dto.Error;
import io.mosip.registration.mdm.dto.ProfileRequest;
import io.mosip.registration.mdm.dto.RCaptureResponse;
import io.mosip.registration.mdm.dto.RCaptureSbiResponse;
import io.mosip.registration.mdm.dto.SbiBioMetricsDataDto;
import io.mosip.registration.mdm.dto.SbiBioMetricsDto;
import io.mosip.registration.mdm.dto.SbiCaptureRequestDto;
import io.mosip.registration.mdm.dto.StreamingRequestDetail;
import io.mosip.registration.mdm.dto.StreamingSbiRequestDetail;

public class SBIServiceResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(SBIServiceResponse.class);	
	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";
	
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
			strJsonRequest.contains(SBIConstant.MOSIP_DISC_VERB) ||
			strJsonRequest.contains(SBIConstant.MOSIP_SBI_DISC_VERB))
		{
			responseJson = processDeviceDicoveryInfo (mockService);
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_INFO_VERB) || strJsonRequest.contains(SBIConstant.MOSIP_INFO_SBI_VERB))
		{
			responseJson = processDeviceInfo (mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_STREAM_VERB))
		{
			responseJson = processLiveStreamInfo (mockService, socket);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_RCAPTURE_VERB))
		{
			responseJson = !mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)? processRCaptureInfo (mockService): processSbiRCaptureInfo(mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_SETPROFILE_VERB))
		{
			responseJson = processSetProfileInfo (mockService);			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_CAPTURE_VERB))
		{
			responseJson = processCaptureInfo (mockService);	
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
             List<DiscoverSBIDto> infoSbiList = new ArrayList<DiscoverSBIDto>();
             if (type == null || type.trim().length() == 0 && !mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION))
    		 {
            	 DiscoverDto discoverInfo = new DiscoverDto ();
            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
            		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
            		}};
            	 discoverInfo.error = errorMap;
            	 infoList.add(discoverInfo);
            	 return objectMapper.writeValueAsString(infoList);
    		 } else if (type == null || type.trim().length() == 0 && mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
            	 DiscoverSBIDto discoverSbiInfo = new DiscoverSBIDto();
            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
            		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
            		}};
            	 Error error = new Error("502", SBIJsonInfo.getErrorDescription(lang, "502"));	
            	 discoverSbiInfo.error = error;
            	 infoSbiList.add(discoverSbiInfo);
            	 return objectMapper.writeValueAsString(infoSbiList);
    		 }
             else if (!type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE).trim().toLowerCase())
        		 && !type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS).trim().toLowerCase()))
             {
            	 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
            		 DiscoverDto discoverInfo = new DiscoverDto ();
                	 Map<String, String> errorMap  = new HashMap<String, String>() {{
                		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
                		}};
                	 discoverInfo.error = errorMap;
                	 infoList.add(discoverInfo);
                	 return objectMapper.writeValueAsString(infoList);
            	 }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
            		 DiscoverSBIDto discoverSbiInfo = new DiscoverSBIDto ();
                	 Map<String, String> errorMap  = new HashMap<String, String>() {{
                		    put("502",  SBIJsonInfo.getErrorDescription(lang, "502"));
                		}};
                	Error error = new Error("502", SBIJsonInfo.getErrorDescription(lang, "502"));
                	 discoverSbiInfo.error = error;
                	 infoSbiList.add(discoverSbiInfo);
                	 return objectMapper.writeValueAsString(infoSbiList);
            	 }
            	 
             }             
             else
             {
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
						 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER).trim().toLowerCase()))
			     {
				 	SBIFingerSlapHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
				 	if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 		deviceHelper.initDeviceDetails();
				 		DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
						if (discoverInfo != null)
							infoList.add(discoverInfo);
				 	}else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 		deviceHelper.initSBIDeviceDetails();
				 		DiscoverSBIDto discoverSbiInfo = deviceHelper.getDiscoverSbiDto();
						if (discoverSbiInfo != null)
							infoSbiList.add(discoverSbiInfo);
				 	}
			     }
				 
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE).trim().toLowerCase()))
				 {
					 SBIFaceHelper deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
					 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
					 		deviceHelper.initDeviceDetails();
					 		DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
							if (discoverInfo != null)
								infoList.add(discoverInfo);
					 	}else if(mockService.getBiometricType().equals(SBIConstant.BIOMETRIC_VERSION)) {
					 		deviceHelper.initSBIDeviceDetails();
					 		DiscoverSBIDto discoverSbiInfo = deviceHelper.getDiscoverSbiDto();
							if (discoverSbiInfo != null)
								infoSbiList.add(discoverSbiInfo);
					 	}
				 }
				
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS).trim().toLowerCase()))
				 {
					 SBIIrisDoubleHelper deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
					 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
					 	deviceHelper.initDeviceDetails();
					 	DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
					 	if (discoverInfo != null)
								infoList.add(discoverInfo);
					 	}else if(mockService.getBiometricType().equals(SBIConstant.BIOMETRIC_VERSION)) {
					 		deviceHelper.initSBIDeviceDetails();
					 		DiscoverSBIDto discoverSbiInfo = deviceHelper.getDiscoverSbiDto();
							if (discoverSbiInfo != null)
								infoSbiList.add(discoverSbiInfo);
					 	}
				 }
				 
				 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
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
				 }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
					 if (infoSbiList != null && infoSbiList.size() > 0)
		        	 {
		            	 return objectMapper.writeValueAsString(infoSbiList);
		        	 }
		             else
		             {
		            	 DiscoverSBIDto discoverSbiInfo = new DiscoverSBIDto();
		            	 Map<String, String> errorMap  = new HashMap<String, String>() {{
		            		    put("503",  SBIJsonInfo.getErrorDescription(lang, "503"));
		            		}};
		            		Error error = new Error("503", SBIJsonInfo.getErrorDescription(lang, "503"));
		            	 discoverSbiInfo.error = error;
		            	 infoSbiList.add(discoverSbiInfo);
		            	 return objectMapper.writeValueAsString(infoSbiList);
		             }
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
        	 DeviceInfoDto deviceInfoDto = null;
        	 DeviceSbiInfoDto deviceSbiInfoDto = null;
             List<DeviceInfoDto> infoList = new ArrayList<DeviceInfoDto> ();
             List<DeviceSbiInfoDto> infoSbiList = new ArrayList<DeviceSbiInfoDto> ();
             SBIDeviceHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
             if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
            	 deviceHelper.initDeviceDetails();
            	 deviceInfoDto = deviceHelper.getDeviceInfoDto(); 
             }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
            	 deviceHelper.initSBIDeviceDetails();
            	 deviceSbiInfoDto = deviceHelper.getDeviceSbiInfoDto(); 
             }
             

             if (deviceInfoDto != null)
				infoList.add(deviceInfoDto);
             else if(deviceSbiInfoDto != null)
            	infoSbiList.add(deviceSbiInfoDto);

			 deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
			 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 deviceHelper.initDeviceDetails();
            	 deviceInfoDto = deviceHelper.getDeviceInfoDto(); 
             }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
            	 deviceHelper.initSBIDeviceDetails();
            	 deviceSbiInfoDto = deviceHelper.getDeviceSbiInfoDto(); 
             }
			 
             if (deviceInfoDto != null)
 				infoList.add(deviceInfoDto);
             else if(deviceSbiInfoDto != null)
             	 infoSbiList.add(deviceSbiInfoDto);            	 

			 deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
			 if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 deviceHelper.initDeviceDetails();
            	 deviceInfoDto = deviceHelper.getDeviceInfoDto(); 
             }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
            	 deviceHelper.initSBIDeviceDetails();
            	 deviceSbiInfoDto = deviceHelper.getDeviceSbiInfoDto(); 
             }
			 
             if (deviceInfoDto != null)
 				infoList.add(deviceInfoDto);
             else if(deviceSbiInfoDto != null)
             	infoSbiList.add(deviceSbiInfoDto);           	 
             
             if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 if (infoList != null && infoList.size() > 0)
	        	 {
	            	 return objectMapper.writeValueAsString(infoList);
	        	 }
	             else
	             {
	            	DeviceInfoDto deviceInfoDtoError = new DeviceInfoDto();
            		Error errorMap = new Error("503", SBIJsonInfo.getErrorDescription(lang, "503"));
            		deviceInfoDtoError.error = errorMap;
	            	infoList.add(deviceInfoDtoError);
	            	return objectMapper.writeValueAsString(infoList);
	             }
			 }else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
				 if (infoSbiList != null && infoSbiList.size() > 0)
	        	 {
	            	 return objectMapper.writeValueAsString(infoSbiList);
	        	 }
	             else
	             {
	            	 DeviceSbiInfoDto deviceSbiInfo = new DeviceSbiInfoDto();
	            		Error error = new Error("503", SBIJsonInfo.getErrorDescription(lang, "503"));
	            		deviceSbiInfo.setError(error);
	            		infoSbiList.add(deviceSbiInfo);
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

	public String processSetProfileInfo (SBIMockService mockService)
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();
        	 ProfileRequest requestObject = (ProfileRequest) getRequestJson (SBIConstant.MOSIP_SETPROFILE_VERB);
        	 if (requestObject != null && requestObject.getProfileId() != null && requestObject.getProfileId().length() > 0)
        	 {
        		 mockService.setProfileId(requestObject.getProfileId());
        		 Error errorinfo = new Error("100", SBIJsonInfo.getErrorDescription(lang, "100")); 
        		 response = objectMapper.writeValueAsString(errorinfo);
        	 }	 
        	 else
        	 {
            	 LOGGER.info("processSetProfileInfo :: ProfileId :: Was  Not SET Check the JSON request :: " );
        		 mockService.setProfileId("Default");
        		 Error errorinfo = new Error("100", SBIJsonInfo.getErrorDescription(lang, "100")); 
        		 response = objectMapper.writeValueAsString(errorinfo);
        	 }
        	 
        	 LOGGER.info("processSetProfileInfo :: ProfileId :: "+  mockService.getProfileId ());
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
            if (mockService.getPurpose().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_AUTH)))
            {
                return SBIJsonInfo.getErrorJson (lang, "601", "");
            }
            if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
	            StreamingRequestDetail requestObject = (StreamingRequestDetail) getRequestJson (SBIConstant.MOSIP_STREAM_VERB);
	            String deviceId = requestObject.getDeviceId();
	            int deviceSubId = Integer.parseInt(requestObject.getDeviceSubId());
	
	            LOGGER.info("processLiveStreamInfo :: deviceId :: "+ deviceId + " :: deviceSubId ::" + deviceSubId);
	
	            deviceHelper = getDeviceInfoForDeviceId (mockService, deviceId);
	            deviceHelper.setDeviceId(deviceId);
	            deviceHelper.setDeviceSubId(deviceSubId);
	            if (deviceId != null && deviceId.trim().length() == 0)
	            {
	                return SBIJsonInfo.getErrorJson (lang, "604", "");
	            }
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
            }else if (mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
            	StreamingSbiRequestDetail requestObject = (StreamingSbiRequestDetail) getSbiRequestJson (SBIConstant.MOSIP_STREAM_VERB);
 	            int deviceSubId = Integer.parseInt(requestObject.getDeviceSubId());
 	
 	            deviceHelper = getSbiDeviceInfoForDeviceId (mockService, requestObject.getSerialNo());
 	            deviceHelper.setDeviceSubId(deviceSubId);
 	            if (deviceHelper.getDeviceSbiInfo().deviceInfo == null)
 	            {
 	                return SBIJsonInfo.getErrorJson (lang, "605", "");
 	            }
 	            if (deviceHelper.getDeviceSbiInfo().deviceInfo != null && !deviceHelper.getDeviceSbiInfo().deviceInfo.getPurpose().trim().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
 	            {
 	                return SBIJsonInfo.getErrorJson (lang, "606", "");
 	            }
 	            if (deviceHelper.getDeviceSbiInfo() != null && !deviceHelper.getDeviceSbiInfo().deviceInfo.getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISREADY))
 	            {
 	                return SBIJsonInfo.getErrorJson (lang, "607", "");
 	            }
            }
            
            deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISUSED);
            deviceHelper.initDevice();
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
            if (!mockService.getPurpose().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION)))
            {
                return SBIJsonInfo.getErrorJson (lang, "709", "");
            }
            
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
            	List<BioMetricsDto> biometrics = getBioMetricsDtoList (lang, requestObject, deviceHelper, deviceSubId, false, mockService);
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
	
	private String processSbiRCaptureInfo(SBIMockService mockService) {
		String response = null;
        String lang = "en";
        String specVersion = "";
        SBIDeviceHelper deviceHelper = null;
        try
        {
            if (!mockService.getPurpose().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION)))
            {
                return SBIJsonInfo.getErrorJson (lang, "709", "");
            }
        	int deviceSubId = 0;
        	SbiCaptureRequestDto requestObject = (SbiCaptureRequestDto) getSbiRequestJson(SBIConstant.MOSIP_RCAPTURE_VERB);
        	List<CaptureRequestDeviceDetailSbiDto> mosipBioRequest = null;
        	// if Null Throw Errors here
        	if (requestObject != null)
        	{
        		mosipBioRequest = requestObject.getBio();
        		if (mosipBioRequest != null && mosipBioRequest.size() > 0)
        		{
                    deviceSubId = Integer.parseInt(requestObject.getBio().get(0).getDeviceSubId());
        		}
        	}

            LOGGER.info("processRCaptureInfo :: deviceSubId ::" + deviceSubId);
            System.out.println("sdkjhd");
            deviceHelper = getSbiDeviceInfoForDeviceId(mockService, mosipBioRequest.get(0).getSerialNo());
            if (deviceHelper.getDeviceSbiInfo().deviceInfo == null)
            {
                return SBIJsonInfo.getErrorJson (lang, "705", "");
            }
            if (deviceHelper.getDeviceSbiInfo().deviceInfo != null && !deviceHelper.getDeviceSbiInfo().deviceInfo.getPurpose().trim().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
            {
                return SBIJsonInfo.getErrorJson (lang, "706", "");
            }
            if (deviceHelper.getCaptureInfo() != null && (deviceHelper.getDeviceSubId() != deviceSubId))
            {
                return SBIJsonInfo.getErrorJson (lang, "702", "");
            }

            if (deviceHelper.getDeviceSbiInfo().deviceInfo != null && deviceHelper.getDeviceSbiInfo().deviceInfo.getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISREADY))
            {
                deviceHelper.initDevice();
                deviceHelper.setDeviceSubId(deviceSubId);
                deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
            }
            else if (deviceHelper.getDeviceSbiInfo().deviceInfo != null && deviceHelper.getDeviceSbiInfo().deviceInfo.getDeviceStatus().trim().equalsIgnoreCase(SBIConstant.DEVICE_STATUS_ISUSED))
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
            		if (!bioType.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)))
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
            	List<SbiBioMetricsDto> sbiBiometrics = getSbiBioMetricsDtoList (lang, requestObject, deviceHelper, deviceSubId, false, mockService);
            	if (sbiBiometrics != null && sbiBiometrics.size() > 0)
            	{
                	RCaptureSbiResponse captureSbiResponse = new RCaptureSbiResponse ();
	            	captureSbiResponse.setBiometrics(sbiBiometrics);

	            	ObjectMapper mapper = new ObjectMapper ();	
	                SerializationConfig config = mapper.getSerializationConfig();
	                config.setSerializationInclusion(Inclusion.NON_NULL);
	                mapper.setSerializationConfig(config);

	                response = mapper.writeValueAsString(captureSbiResponse);
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

	private String processCaptureInfo(SBIMockService mockService) {
		String response = null;
        String lang = "en";
        String specVersion = "";
        SBIDeviceHelper deviceHelper = null;
        try
        {
            if (!mockService.getPurpose().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_AUTH)))
            {
                return SBIJsonInfo.getErrorJson (lang, "809", "");
            }

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

            LOGGER.info("processCaptureInfo :: deviceId :: "+ deviceId + " :: deviceSubId ::" + deviceSubId);

            if (deviceId != null && deviceId.trim().length() == 0)
            {
                return SBIJsonInfo.getErrorJson (lang, "804", "");
            }
            
            deviceHelper = getDeviceInfoForDeviceId (mockService, deviceId);
            if (deviceHelper.getDeviceInfo() == null)
            {
                return SBIJsonInfo.getErrorJson (lang, "805", "");
            }
            if (deviceHelper.getDeviceInfo() != null && !deviceHelper.getDeviceInfo().getPurpose().trim().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH))
            {
                return SBIJsonInfo.getErrorJson (lang, "806", "");
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
                    return SBIJsonInfo.getErrorJson (lang, "803", "");
            }
            
            String bioType = mosipBioRequest.get(0).getType();
            String [] bioSubType = mosipBioRequest.get(0).getBioSubType();// Bio Subtype
            int timeout = Integer.parseInt(requestObject.getTimeout()+ "");
            int requestScore = Integer.parseInt(mosipBioRequest.get(0).getRequestedScore() + "");
            int bioCount = Integer.parseInt(mosipBioRequest.get(0).getCount() + "");
            
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
            		
        			deviceHelper.getCaptureInfo().setBioCount(bioCount);
        			deviceHelper.getCaptureInfo().setBioSubType(bioSubType);
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
            		
                    returnCode = deviceHelper.getBioCapture(true);

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
            	List<BioMetricsDto> biometrics = getBioMetricsDtoList (lang, requestObject, deviceHelper, deviceSubId, true, mockService);
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
            response = SBIJsonInfo.getCaptureErrorJson (specVersion, lang, "810", "", true);
            LOGGER.error("processCaptureInfo", ex);
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

	private List<BioMetricsDto> getBioMetricsDtoList (String lang, CaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, int deviceSubId, boolean isForAuthenication, SBIMockService mockService) throws JsonGenerationException, JsonMappingException, IOException
	{
        List<BioMetricsDto> biometrics = new ArrayList<BioMetricsDto> ();
    	String specVersion = requestObject.getSpecVersion();
    	String transactionId = requestObject.getTransactionId();
    	int captureScore = 40; // SET MANUALLY
    	int requestScore = requestObject.getBio().get(0).getRequestedScore();
    	int bioCount = requestObject.getBio().get(0).getCount();
    	String bioType = requestObject.getBio().get(0).getType();
        String [] bioExceptions = requestObject.getBio().get(0).getException();// Bio exceptions
        String [] bioSubType = requestObject.getBio().get(0).getBioSubType();// Bio SubTypes

    	String previousHash = requestObject.getBio().get(0).getPreviousHash() + "".trim ();  
    	if (!isForAuthenication)
    	{
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
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLM(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLR(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLL(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRM(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRR(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRL(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLT(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRT(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
        		if (bioExceptions != null && bioExceptions.length > 0)
        			isExceptionPhoto = true;

        		if (!isExceptionPhoto)
    			{
    				if (captureInfo.getBioValueFace() != null && captureInfo.getBioValueFace().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
                                captureInfo.getBioValueFace(), captureScore, requestScore, "", "100", isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
        		else
    			{
    				if (captureInfo.getBioValueExceptionPhoto() != null && captureInfo.getBioValueExceptionPhoto().length() > 0)
    				{
        				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
                                captureInfo.getBioValueExceptionPhoto(), captureScore, requestScore, "", "100", isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
        	}
    	}
    	else if (isForAuthenication)
    	{
    		SBIBioSubTypeInfo bioSubTypeInfo = new SBIBioSubTypeInfo ();
    		bioSubTypeInfo.initBioSubType(bioSubType);
    		
        	// For Finger Slap
        	if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
        			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
        	{
        		SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo)deviceHelper.getCaptureInfo();
        		if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_INDEX))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_INDEX : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftMiddle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_MIDDLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_MIDDLE : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftRing() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_RING))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftRing() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_RING : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftLittle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_LITTLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftLittle() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_LITTLE: SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
        		}
        		else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_INDEX))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_INDEX : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightMiddle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_MIDDLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_MIDDLE : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightRing() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_RING))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightRing() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_RING : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightLittle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_LITTLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightLittle() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_LITTLE: SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
        		}
        		else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftThumb() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_THUMB))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftThumb() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_THUMB : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightThumb() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_THUMB))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightThumb() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_THUMB : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
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
        			if (bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked)
        			{
            			String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS);
        				if (bioData != null && bioData.length() > 0)
        				{
            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
        			if (bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked)
        			{
            			String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS);
        				if (bioData != null && bioData.length() > 0)
        				{
            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	biometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_IRIS))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_IRIS))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	biometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	biometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}    		
        	}    
        	// For Face
        	else if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
        			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
        	{
        		SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo)deviceHelper.getCaptureInfo();
        		String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_UNKNOWN);
				if (bioData != null && bioData.length() > 0)
				{
    				BioMetricsDto bioDto = getBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
    						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                    if (bioDto != null)
                    {
                    	biometrics.add(bioDto);
                        previousHash = bioDto.getHash();
                    }
				}
				else
				{
					BioMetricsDto bioDto = getBiometricErrorData (lang, specVersion, isForAuthenication);
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
	
	private List<SbiBioMetricsDto> getSbiBioMetricsDtoList (String lang, SbiCaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, int deviceSubId, boolean isForAuthenication, SBIMockService mockService) throws JsonGenerationException, JsonMappingException, IOException
	{
        List<SbiBioMetricsDto> sbiBiometrics = new ArrayList<SbiBioMetricsDto> ();
    	String specVersion = requestObject.getSpecVersion();
    	String transactionId = requestObject.getTransactionId();
    	int captureScore = 40; // SET MANUALLY
    	int requestScore = requestObject.getBio().get(0).getRequestedScore();
    	int bioCount = requestObject.getBio().get(0).getCount();
    	String bioType = requestObject.getBio().get(0).getType();
        String [] bioExceptions = requestObject.getBio().get(0).getException();// Bio exceptions
        String [] bioSubType = requestObject.getBio().get(0).getBioSubType();// Bio SubTypes

    	String previousHash = requestObject.getBio().get(0).getPreviousHash() + "".trim ();  
    	if (!isForAuthenication)
    	{
        	// For Finger Slap
        	if (deviceHelper.getDigitalSbiId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
        			deviceHelper.getDigitalSbiId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
        	{
        		SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo)deviceHelper.getCaptureInfo();
        		SBIFingerSlapBioExceptionInfo bioExceptionInfo = (SBIFingerSlapBioExceptionInfo)deviceHelper.getCaptureInfo().getBioExceptionInfo();
        		if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
        		{
        			if (bioExceptionInfo.getChkMissingLeftIndex() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0)
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricData(transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_INDEX,
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingLeftMiddle() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueLM() != null && captureInfo.getBioValueLM().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_MIDDLE,
                                    captureInfo.getBioValueLM(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingLeftRing() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueLR() != null && captureInfo.getBioValueLR().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_RING,
                                    captureInfo.getBioValueLR(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingLeftLittle() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueLL() != null && captureInfo.getBioValueLL().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_LITTLE,
                                    captureInfo.getBioValueLL(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
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
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_INDEX,
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingRightMiddle() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueRM() != null && captureInfo.getBioValueRM().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_MIDDLE,
                                    captureInfo.getBioValueRM(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingRightRing() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueRR() != null && captureInfo.getBioValueRR().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_RING,
                                    captureInfo.getBioValueRR(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingRightLittle() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueRL() != null && captureInfo.getBioValueRL().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_LITTLE,
                                    captureInfo.getBioValueRL(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
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
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_THUMB,
                                    captureInfo.getBioValueLT(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingRightThumb() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueRT() != null && captureInfo.getBioValueRT().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_THUMB,
                                    captureInfo.getBioValueRT(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
    			}
        	}
        	// For IRIS DOUBLE
        	else if (deviceHelper.getDigitalSbiId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
        			deviceHelper.getDigitalSbiId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
        	{
        		SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo)deviceHelper.getCaptureInfo();
        		SBIIrisDoubleBioExceptionInfo bioExceptionInfo = (SBIIrisDoubleBioExceptionInfo)deviceHelper.getCaptureInfo().getBioExceptionInfo();

        		if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
    			{
        			if (bioExceptionInfo.getChkMissingLeftIris() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS,
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
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
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS,
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
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
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS,
                                    captureInfo.getBioValueLI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
        			if (bioExceptionInfo.getChkMissingRightIris() == SBICheckState.Unchecked)
        			{
        				if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS,
                                    captureInfo.getBioValueRI(), captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
    			}    		
        	}    
        	// For Face
        	else if (deviceHelper.getDigitalSbiId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
        			deviceHelper.getDigitalSbiId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
        	{
        		SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo)deviceHelper.getCaptureInfo();
        		
        		boolean isExceptionPhoto = false;    		
        		if (bioExceptions != null && bioExceptions.length > 0)
        			isExceptionPhoto = true;

        		if (!isExceptionPhoto)
    			{
    				if (captureInfo.getBioValueFace() != null && captureInfo.getBioValueFace().length() > 0)
    				{
        				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType,
        						requestObject.bio.get(0).getBioSubType()[0], captureInfo.getBioValueFace(), captureScore, requestScore, "", "100", isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
        		else
    			{
    				if (captureInfo.getBioValueExceptionPhoto() != null && captureInfo.getBioValueExceptionPhoto().length() > 0)
    				{
        				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
                                captureInfo.getBioValueExceptionPhoto(), captureScore, requestScore, "", "100", isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add(bioDto);
                            previousHash = bioDto.getHash();
                        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}
        	}
    	}
    	else if (isForAuthenication)
    	{
    		SBIBioSubTypeInfo bioSubTypeInfo = new SBIBioSubTypeInfo ();
    		bioSubTypeInfo.initBioSubType(bioSubType);
    		
        	// For Finger Slap
        	if (deviceHelper.getDigitalSbiId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) &&
        			deviceHelper.getDigitalSbiId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)))
        	{
        		SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo)deviceHelper.getCaptureInfo();
        		if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_INDEX))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_INDEX : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftMiddle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_MIDDLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_MIDDLE : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftRing() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_RING))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftRing() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_RING : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftLittle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_LITTLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftLittle() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_LITTLE: SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
        		}
        		else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_INDEX))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_INDEX : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightMiddle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_MIDDLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIndex() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_MIDDLE : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightRing() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_RING))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightRing() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_RING : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightLittle() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_LITTLE))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightLittle() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_LITTLE: SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
        		}
        		else if (deviceSubId == SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB)
        		{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftThumb() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_THUMB))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftThumb() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_THUMB : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightThumb() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_THUMB))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightThumb() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_THUMB : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
        		}
        	}
        	// For IRIS DOUBLE
        	else if (deviceHelper.getDigitalSbiId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) &&
        			deviceHelper.getDigitalSbiId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)))
        	{
        		SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo)deviceHelper.getCaptureInfo();
        		SBIIrisDoubleBioExceptionInfo bioExceptionInfo = (SBIIrisDoubleBioExceptionInfo)deviceHelper.getCaptureInfo().getBioExceptionInfo();

        		if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_LEFT)
    			{        		
        			if (bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked)
        			{
            			String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_LEFT_IRIS);
        				if (bioData != null && bioData.length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
    			}
    			else if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_RIGHT)
    			{
        			if (bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked)
        			{
            			String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_RIGHT_IRIS);
        				if (bioData != null && bioData.length() > 0)
        				{
            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add(bioDto);
                                previousHash = bioDto.getHash();
                            }
        				}
        				else
        				{
        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                            if (bioDto != null)
                            {
                            	sbiBiometrics.add (bioDto);
                                previousHash = bioDto.getHash();
                            }
                        }
        			}
    			}
    			else if (deviceSubId == SBIConstant.DEVICE_IRIS_SUB_TYPE_ID_BOTH)
    			{
    				HashMap<String, String> biometricData = captureInfo.getBiometricData();
    				if (biometricData != null && biometricData.size() > 0)
    				{
    					int bioCounter = 1;
    					for (Map.Entry<String, String> pair: biometricData.entrySet()) {
    						if (bioCounter > bioCount)
    							break;
    			            
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_LEFT_IRIS))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkLeftIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_LEFT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}
    	        			
    	        			if ((bioSubTypeInfo.getChkUnknown() == SBICheckState.Checked || bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked)
    	        					&& pair.getKey().equalsIgnoreCase(SBIConstant.BIO_NAME_RIGHT_IRIS))
    	        			{
    	            			String bioData = pair.getValue();
    	        				if (bioData != null && bioData.length() > 0)
    	        				{
    	            				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, (bioSubTypeInfo.getChkRightIris() == SBICheckState.Checked ? SBIConstant.BIO_NAME_RIGHT_IRIS : SBIConstant.BIO_NAME_UNKNOWN),
    	            						bioData, captureScore, requestScore, "", "100", isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add(bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	        				}
    	        				else
    	        				{
    	        					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
    	                            if (bioDto != null)
    	                            {
    	                            	sbiBiometrics.add (bioDto);
    	                                previousHash = bioDto.getHash();
    	                            }
    	                        }
    	        			}

    	        			bioCounter ++;
    			        }
    				}
    				else
    				{
    					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                        if (bioDto != null)
                        {
                        	sbiBiometrics.add (bioDto);
                            previousHash = bioDto.getHash();
                        }
                    }
    			}    		
        	}    
        	// For Face
        	else if (deviceHelper.getDigitalId().getType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) &&
        			deviceHelper.getDigitalId().getDeviceSubType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)))
        	{
        		SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo)deviceHelper.getCaptureInfo();
        		String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_UNKNOWN);
				if (bioData != null && bioData.length() > 0)
				{
    				SbiBioMetricsDto bioDto = getSbiBiometricData (transactionId, requestObject, deviceHelper, previousHash, bioType, null,
    						bioData, captureScore, requestScore, "", "100", isForAuthenication);
                    if (bioDto != null)
                    {
                    	sbiBiometrics.add(bioDto);
                        previousHash = bioDto.getHash();
                    }
				}
				else
				{
					SbiBioMetricsDto bioDto = getSbiBiometricErrorData (lang, specVersion, isForAuthenication);
                    if (bioDto != null)
                    {
                    	sbiBiometrics.add (bioDto);
                        previousHash = bioDto.getHash();
                    }
                }
        	}
    	}
    	return sbiBiometrics;
	}
	
	private BioMetricsDto getBiometricData (String transactionId, CaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, 
			String previousHash, String bioType, String bioSubType, String bioValue, 
			int qualityScore, int qualityRequestScore, String lang, String errorCode, boolean isUsedForAuthenication) throws JsonGenerationException, JsonMappingException, IOException
    {
		Map<String, String> cryptoResult = null;
		DeviceInfo deviceInfo = deviceHelper.getDeviceInfo();
		
		BioMetricsDto biometric = new BioMetricsDto ();
        biometric.setSpecVersion(requestObject.getSpecVersion());

        biometric.setError(new Error (errorCode, SBIJsonInfo.getErrorDescription (lang, errorCode)));

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

        if (isUsedForAuthenication == false)
        {
            biometricData.setBioValue(bioValue);
        }
        else
        {
			try {
				X509Certificate certificate = new JwtUtility().getCertificateToEncryptCaptureBioValue();
				PublicKey publicKey = certificate.getPublicKey();
				cryptoResult = CryptoUtility.encrypt(publicKey,
						java.util.Base64.getUrlDecoder().decode(bioValue), transactionId);
				
				biometricData.setTimestamp(cryptoResult.get("TIMESTAMP"));
				biometricData.setBioValue(cryptoResult.containsKey("ENC_DATA") ? 
						cryptoResult.get("ENC_DATA") : null);		
				biometric.setSessionKey(cryptoResult.get("ENC_SESSION_KEY"));
				biometric.setThumbprint(CryptoUtil.encodeBase64(JwtUtility.getCertificateThumbprint(certificate)));
			} catch (Exception ex) {
                LOGGER.error("getBiometricData :: encrypt :: ", ex);
				// TODO Auto-generated catch block
                ex.printStackTrace();
			}
        }

        biometricData.setRequestedScore(qualityRequestScore + "");
        biometricData.setQualityScore(80 + "");
        biometricData.setTransactionId(transactionId);
        biometricData.setTimestamp(LocalDateTime.now().toString());
        
        ObjectMapper mapper = new ObjectMapper ();	
        SerializationConfig config = mapper.getSerializationConfig();
        config.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationConfig(config);

		String currentBioData = mapper.writeValueAsString(biometricData);

        //base64 signature of the data block. base64 signature of the hash element
        String dataBlockSignBase64 = deviceHelper.getSignBioMetricsDataDto (deviceHelper.getDeviceType(), deviceHelper.getDeviceSubType(), currentBioData);
        biometric.setData (dataBlockSignBase64);

        String previousDataHashSHA256Hex = "";
        if (previousHash == null || previousHash.trim().length() == 0)
        {
            byte [] previousDataByteArr = StringHelper.toUtf8ByteArray (previousHash);
            previousDataHashSHA256Hex = HMACUtils.digestAsPlainText (HMACUtils.generateHash(previousDataByteArr));
        }
        else
        {
            previousDataHashSHA256Hex = previousHash;
        }

        byte [] currentDataByteArr = StringHelper.toUtf8ByteArray (currentBioData);
        String currentDataHashSHA256Hex = HMACUtils.digestAsPlainText (HMACUtils.generateHash (currentDataByteArr));

        String finalDataHash = previousDataHashSHA256Hex + currentDataHashSHA256Hex;
        byte [] finalDataByteArr = StringHelper.toUtf8ByteArray (finalDataHash);
        biometric.setHash(HMACUtils.digestAsPlainText (HMACUtils.generateHash (finalDataByteArr)));

        return biometric;
    }
	
	private SbiBioMetricsDto getSbiBiometricData (String transactionId, SbiCaptureRequestDto requestObject, SBIDeviceHelper deviceHelper, 
			String previousHash, String bioType, String bioSubType, String bioValue, 
			int qualityScore, int qualityRequestScore, String lang, String errorCode, boolean isUsedForAuthenication) throws JsonGenerationException, JsonMappingException, IOException
    {
		DeviceSBIInfo deviceInfo = deviceHelper.getDeviceSbiInfo();
		
		SbiBioMetricsDto sbiBiometric = new SbiBioMetricsDto();
		sbiBiometric.setSpecVersion(requestObject.getSpecVersion());

		sbiBiometric.setError(new Error(errorCode, SBIJsonInfo.getErrorDescription (lang, errorCode)));

        SbiBioMetricsDataDto sbiBiometricData = new SbiBioMetricsDataDto();
        sbiBiometricData.setDigitalId(deviceInfo.deviceInfo.getDigitalId());
        sbiBiometricData.setDeviceServiceVersion(deviceInfo.deviceInfo.getServiceVersion());
        sbiBiometricData.setBioType(bioType);
        sbiBiometricData.setBioSubType(bioSubType);

        sbiBiometricData.setPurpose(requestObject.getPurpose());
        sbiBiometricData.setEnv(requestObject.getEnv());

        if (isUsedForAuthenication == false)
        {
        	sbiBiometricData.setBioValue(bioValue);
        }
        else
        {
			try {
				X509Certificate certificate = new JwtUtility().getCertificateToEncryptCaptureBioValue();
				PublicKey publicKey = certificate.getPublicKey();
				Map<String, String> cryptoResult = CryptoUtility.encrypt(publicKey,
						java.util.Base64.getUrlDecoder().decode(bioValue), transactionId);
				
				sbiBiometricData.setTimestamp(cryptoResult.get("TIMESTAMP"));
				sbiBiometricData.setBioValue(cryptoResult.containsKey("ENC_DATA") ? 
						cryptoResult.get("ENC_DATA") : null);
			} catch (Exception ex) {
                LOGGER.error("getBiometricData :: encrypt :: ", ex);
				// TODO Auto-generated catch block
                ex.printStackTrace();
			}
        }

        sbiBiometricData.setRequestedScore(qualityRequestScore + "");
        sbiBiometricData.setQualityScore(80 + "");
        sbiBiometricData.setTransactionId(transactionId);

        ObjectMapper mapper = new ObjectMapper ();	
        SerializationConfig config = mapper.getSerializationConfig();
        config.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationConfig(config);

		String currentBioData = mapper.writeValueAsString(sbiBiometricData);

        //base64 signature of the data block. base64 signature of the hash element
        String dataBlockSignBase64 = deviceHelper.getSignBioMetricsDataDto (deviceHelper.getDeviceType(), deviceHelper.getDeviceSubType(), currentBioData);
        sbiBiometric.setData (dataBlockSignBase64);

        String previousDataHashSHA256Hex = "";
        if (previousHash == null || previousHash.trim().length() == 0)
        {
            byte [] previousDataByteArr = StringHelper.toUtf8ByteArray (previousHash);
            previousDataHashSHA256Hex = HMACUtils.digestAsPlainText (HMACUtils.generateHash(previousDataByteArr));
        }
        else
        {
            previousDataHashSHA256Hex = previousHash;
        }

        byte [] currentDataByteArr = StringHelper.toUtf8ByteArray (currentBioData);
        String currentDataHashSHA256Hex = HMACUtils.digestAsPlainText (HMACUtils.generateHash (currentDataByteArr));

        String finalDataHash = previousDataHashSHA256Hex + currentDataHashSHA256Hex;
        byte [] finalDataByteArr = StringHelper.toUtf8ByteArray (finalDataHash);
        sbiBiometric.setHash(HMACUtils.digestAsPlainText (HMACUtils.generateHash (finalDataByteArr)));

        return sbiBiometric;
    }

	private BioMetricsDto getBiometricErrorData (String lang, String specVersion, boolean isForAuthenication)
	{
		String errorCode = "701"; 
	    BioMetricsDto biometric = new BioMetricsDto ();
	    biometric.setSpecVersion(specVersion);
	    biometric.setData("");
	    biometric.setHash("");
	    if (isForAuthenication)
	    {
	    	errorCode = "801";
	        biometric.setSessionKey("");
	        biometric.setThumbprint("");
	    }

	    biometric.setError(new Error (errorCode, (SBIJsonInfo.getErrorDescription (lang, errorCode)).trim()));
	    
	    return biometric;
	}
	
	private SbiBioMetricsDto getSbiBiometricErrorData (String lang, String specVersion, boolean isForAuthenication)
	{
		String errorCode = "701"; 
	    SbiBioMetricsDto sbiBiometric = new SbiBioMetricsDto ();
	    sbiBiometric.setSpecVersion(specVersion);
	    sbiBiometric.setData("");
	    sbiBiometric.setHash("");
	    if (isForAuthenication)
	    {
	    	errorCode = "801";
	    }

	    sbiBiometric.setError(new Error (errorCode, (SBIJsonInfo.getErrorDescription (lang, errorCode)).trim()));
	    
	    return sbiBiometric;
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
	
private SBIDeviceHelper getSbiDeviceInfoForDeviceId(SBIMockService mockService, String serialNo) {
		
        SBIDeviceHelper deviceHelper = (SBIFingerSlapHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP));
        if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
        	if (deviceHelper.getDeviceInfo() != null) {
        		deviceHelper.initDeviceDetails();
            	return deviceHelper; 
            }
        }
        else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
        	if (deviceHelper.getDeviceSbiInfo() != null && deviceHelper.getDeviceSbiInfo().deviceInfo.getSerialNo().trim().equals(serialNo.trim())) {
        		deviceHelper.initSBIDeviceDetails();
            	return deviceHelper; 
            }
        }
        
        deviceHelper = (SBIFaceHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE));
        if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
        	if (deviceHelper.getDeviceInfo() != null) {
        		deviceHelper.initDeviceDetails();
            	return deviceHelper; 
            }
        }
        else if(mockService.getBiometricType().equals(SBIConstant.BIOMETRIC_VERSION)){
        	if (deviceHelper.getDeviceSbiInfo() != null && deviceHelper.getDeviceSbiInfo().deviceInfo.getSerialNo().trim().equals(serialNo.trim())) {
        		deviceHelper.initSBIDeviceDetails();
            	return deviceHelper; 
            }
        }

        deviceHelper = (SBIIrisDoubleHelper) mockService.getDeviceHelper(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE));
        if(!mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)) {
        	if (deviceHelper.getDeviceInfo() != null) {
        		deviceHelper.initDeviceDetails();
            	return deviceHelper; 
            }
        }
        else if(mockService.getBiometricVersion().equals(SBIConstant.BIOMETRIC_VERSION)){
        	if (deviceHelper.getDeviceSbiInfo() != null && deviceHelper.getDeviceSbiInfo().deviceInfo.getSerialNo().trim().equals(serialNo.trim())) {
        		deviceHelper.initSBIDeviceDetails();
            	return deviceHelper; 
            }
        }

        return null;
	}

	public Object getRequestJson (String methodVerb)
    {
        if (getRequest () != null && getRequest().indexOf("{") >= 0)
        {
            try
            {
            	com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            	mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_DISC_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), DeviceDiscoveryRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_STREAM_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), StreamingRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_RCAPTURE_VERB)) {
        			mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), CaptureRequestDto.class);
        		}
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
	
	public Object getSbiRequestJson (String methodVerb)
    {
        if (getRequest () != null && getRequest().indexOf("{") >= 0)
        {
            try
            {
            	com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            	mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_DISC_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), DeviceDiscoveryRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_STREAM_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), StreamingSbiRequestDetail.class);
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_RCAPTURE_VERB)) {
        			mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), SbiCaptureRequestDto.class);
        		}
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
	
	
	public PublicKey getPublicKeyToEncryptCaptureBioValue() throws Exception {
		String certificate = getPublicKeyFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
				new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));

		return x509Certificate.getPublicKey();
	}
	
	public String getThumbprint() throws Exception {
		String certificate = getPublicKeyFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(
				new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));
		String thumbprint = CryptoUtil.computeFingerPrint(x509Certificate.getEncoded(), null);

		return thumbprint;
	}

	public String getPublicKeyFromIDA() {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE,
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.appid"),
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.clientid"),
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.secretkey"),
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder()
				.url(ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.server.url"))
				.post(body)
				.build();
		try {
			Response response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				String authToken = response.header("authorization");

				Request idarequest = new Request.Builder()
						.header("cookie", "Authorization="+authToken)
						.url(ApplicationPropertyHelper.getPropertyKeyValue("mosip.ida.server.url"))
						.get()
						.build();

				Response idaResponse = new OkHttpClient().newCall(idarequest).execute();
				if(idaResponse.isSuccessful()) {
					JSONObject jsonObject = new JSONObject(idaResponse.body().string());
					jsonObject = jsonObject.getJSONObject("response");
					return jsonObject.getString("certificate");
				}
			}

		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
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
