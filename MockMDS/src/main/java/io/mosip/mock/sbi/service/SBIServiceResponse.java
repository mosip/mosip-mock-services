package io.mosip.mock.sbi.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.biometric.provider.JwtUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.DatetimeHelper;
import io.mosip.mock.sbi.util.FileHelper;
import io.mosip.mock.sbi.util.StringHelper;
import io.mosip.registration.mdm.dto.DeviceDiscoveryRequestDetailDto;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceInfoDto;
import io.mosip.registration.mdm.dto.DigitalId;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.ErrorInfo;

public class SBIServiceResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(SBIServiceResponse.class);	
	
	protected int port = 0;
	protected String request = "";
	static Semaphore semaphore = new Semaphore (1);
	
	public SBIServiceResponse (int port)
	{
		setPort (port);
	}
	public String getServiceresponse (Socket socket, String strJsonRequest)
    {
		String responseJson = "";
		setRequest (strJsonRequest);

		if (strJsonRequest.contains(SBIConstant.MOSIP_POST_VERB) ||
			strJsonRequest.contains(SBIConstant.MOSIP_GET_VERB) ||
			strJsonRequest.contains(SBIConstant.MOSIP_DISC_VERB))
		{
			responseJson = processDeviceDicoveryInfo ();
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_INFO_VERB))
		{
			responseJson = processDeviceInfo ();			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_STREAM_VERB))
		{
			
		}
		else if (strJsonRequest.contains(SBIConstant.MOSIP_RCAPTURE_VERB))
		{
			
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
	
	public String processDeviceDicoveryInfo ()
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();
        	 DeviceDiscoveryRequestDetailDto requestObject = (DeviceDiscoveryRequestDetailDto) getRequestJson (SBIConstant.MOSIP_DISC_VERB);
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
				         
				 	DigitalId digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER);
				 	if (digitalId != null)
				 	{
						DiscoverDto discoverInfo = getDiscoverInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, digitalId);
						if (discoverInfo != null)
							infoList.add(discoverInfo);
				 	}
			     }
				 
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE).trim().toLowerCase()))
				 {
					 DigitalId digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE);
				 	if (digitalId != null)
				 	{
						 DiscoverDto discoverInfo = getDiscoverInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE, digitalId);
						 if (discoverInfo != null)
							 infoList.add(discoverInfo);
				 	}
				 }
				
				 if (type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE).trim().toLowerCase())
					 || type.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS).trim().toLowerCase()))
				 {
					 DigitalId digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS);
				 	if (digitalId != null)
				 	{					 
						 DiscoverDto discoverInfo = getDiscoverInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS, digitalId);
						 if (discoverInfo != null)
							 infoList.add(discoverInfo);
				 	}
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
         return null;
    }
	
	public String processDeviceInfo ()
    {
		 String response = null;
         String lang = "en";
         try
         {
        	 ObjectMapper objectMapper = new ObjectMapper();

             List<DeviceInfoDto> infoList = new ArrayList<DeviceInfoDto> ();
             DigitalId digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER);
             if (digitalId != null)
             {
                 DeviceInfo deviceInfo = getDeviceInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, digitalId);
                 if (deviceInfo != null)
                 {
	                 DeviceInfoDto deviceInfoDto = getDeviceInfoDto (SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, deviceInfo);
	                 if (deviceInfoDto != null)
	    				infoList.add(deviceInfoDto);            	 
                 }
             }
			 
			 digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE);
             if (digitalId != null)
             {
            	 DeviceInfo deviceInfo = getDeviceInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE, digitalId);
                 if (deviceInfo != null)
                 {
                	 DeviceInfoDto deviceInfoDto = getDeviceInfoDto (SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE, deviceInfo);
					 if (deviceInfoDto != null)
						 infoList.add(deviceInfoDto);
                 }
             }		
             
			 digitalId = getDigitalId (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS);
             if (digitalId != null)
             {
            	 DeviceInfo deviceInfo = getDeviceInfo (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS, digitalId);
                 if (deviceInfo != null)
                 {
                	 DeviceInfoDto deviceInfoDto = getDeviceInfoDto (SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS, deviceInfo);
					 if (deviceInfoDto != null)
						 infoList.add(deviceInfoDto);
                 }
             }
			 if (infoList != null && infoList.size() > 0)
        	 {
            	 return objectMapper.writeValueAsString(infoList);
        	 }
             else
             {
            	 DeviceInfoDto deviceInfoDto = new DeviceInfoDto ();
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
         return null;
    }

	private DigitalId getDigitalId(String biometricType) {
		DigitalId digitalId = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DIGITALID_JSON);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DIGITALID_JSON);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DIGITALID_JSON);
			}
			if (FileHelper.exists(fileName)) 
			{
				File file = new File(fileName);
				digitalId = objectMapper.readValue(file, DigitalId.class);
				if (digitalId != null)
				{
					digitalId.setDateTime(DatetimeHelper.getISO8601CurrentDate());
				}
				
				return digitalId;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDigitalId :: BiometricType::" + biometricType, ex);
		}
		return null;
	}

	private DiscoverDto getDiscoverInfo(String biometricType, DigitalId digitalId) {		
		DiscoverDto discoverDto = null;
		String fileName = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEDEISCOVERYINFO_JSON);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEDEISCOVERYINFO_JSON);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEDEISCOVERYINFO_JSON);
			}

			if (FileHelper.exists(fileName)) 
			{
				File file = new File(fileName);
				discoverDto = objectMapper.readValue(file, DiscoverDto.class);
				if (discoverDto != null)
				{
					discoverDto.setDigitalId(getUnsignedDigitalId (digitalId, true));
					discoverDto.setCallbackId("http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort () + "/");
				}
				
				return discoverDto;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDiscoverInfo :: BiometricType::" + biometricType, ex);
		}
		return null;
	}
	
	private DeviceInfo getDeviceInfo(String biometricType, DigitalId digitalId) {
		DeviceInfo deviceInfo = null;
		String fileName = null;
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FINGER_SLAP_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_FACE_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
			{
				fileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_IRIS_DOUBLE_DEVICEINFO_JSON);
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
			}

			if (FileHelper.exists(fileName) && FileHelper.exists(keyStoreFileName)) 
			{
				File file = new File(fileName);
				KeyStore keystore = loadKeyStore (keyStoreFileName, keyPwd);
				
				PrivateKey key = (PrivateKey)keystore.getKey(keyAlias, keyPwd.toCharArray());

	            /* Get certificate of public key */
	            java.security.cert.Certificate cert = keystore.getCertificate(keyAlias); 

	            /* Here it prints the public key*/
	            //LOGGER.Info("Public Key:");
	            //LOGGER.Info(cert.getPublicKey());

	            /* Here it prints the private key*/
	            //LOGGER.Info("\nPrivate Key:");
	            //LOGGER.Info(key);
	            
				deviceInfo = objectMapper.readValue(file, DeviceInfo.class);
				if (deviceInfo != null)
				{
					deviceInfo.setDigitalId(getUnsignedDigitalId (digitalId, false));
					deviceInfo.setCallbackId("http://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort () + "/");
					deviceInfo.setDigitalId(getSignedDigitalId (deviceInfo.getDigitalId(), key, cert));
				}
        		return deviceInfo;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfo :: BiometricType::" + biometricType, ex);
		}
		return null;
	}

	private DeviceInfoDto getDeviceInfoDto(String biometricType, DeviceInfo deviceInfo) {
		DeviceInfoDto deviceInfoDto = new DeviceInfoDto ();
		String keyStoreFileName = null;
		String keyAlias = null;
		String keyPwd = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FINGER_SLAP_KEYSTORE_PWD);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_FACE_KEYSTORE_PWD);
			}
			else if (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
			{
				keyStoreFileName = FileHelper.getCanonicalPath () + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_FILE_NAME);
				keyAlias = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEY_ALIAS);
				keyPwd = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_STREAM_IRIS_DOUBLE_KEYSTORE_PWD);
			}

			if (FileHelper.exists(keyStoreFileName)) 
			{
				String strDeviceInfo = objectMapper.writeValueAsString(deviceInfo);
				KeyStore keystore = loadKeyStore (keyStoreFileName, keyPwd);
				
				PrivateKey key = (PrivateKey)keystore.getKey(keyAlias, keyPwd.toCharArray());

	            /* Get certificate of public key */
	            java.security.cert.Certificate cert = keystore.getCertificate(keyAlias); 

	            /* Here it prints the public key*/
	            //LOGGER.Info("Public Key:");
	            //LOGGER.Info(cert.getPublicKey());

	            /* Here it prints the private key*/
	            //LOGGER.Info("\nPrivate Key:");
	            //LOGGER.Info(key);
	            deviceInfoDto.setDeviceInfo(JwtUtility.getJwt(strDeviceInfo.getBytes("UTF-8"), key, (X509Certificate) cert));
           	 	deviceInfoDto.setError(new ErrorInfo ("100", SBIJsonInfo.getErrorDescription("en", "100"))); 
    		
        		return deviceInfoDto ;
			}	
		} catch (Exception ex) {
        	LOGGER.error("getDeviceInfo :: BiometricType::" + biometricType, ex);
		}
		return null;
	}

	public String getUnsignedDigitalId (DigitalId digitalId, boolean isBase64URLEncoded)
    {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (isBase64URLEncoded)
			{
				return StringHelper.base64UrlEncode (objectMapper.writeValueAsString(digitalId));
			}
			else
			{
				return objectMapper.writeValueAsString(digitalId);
			}
		} catch (Exception ex) {
        	LOGGER.error("getUnsignedDigitalId :: " , ex);
		}
		return null;
    }

	public String getSignedDigitalId (String digitalId, PrivateKey privateKey, Certificate cert)
    {
		try {
			return JwtUtility.getJwt (digitalId.getBytes("UTF-8"), privateKey, (X509Certificate) cert);
		} catch (Exception ex) {
        	LOGGER.error("getSignedDigitalId :: " , ex);
		}
		return null;
    }
	
	public KeyStore loadKeyStore(String fileName, String keystorePwd) throws Exception {
	    File file = new File(fileName);
	    KeyStore keyStore = KeyStore.getInstance("JKS");
	    if (file.exists()) {
	        // if exists, load
	        keyStore.load(new FileInputStream(file), keystorePwd.toCharArray());
	    } else {
	        // if not exists, create
	        keyStore.load(null, null);
	        keyStore.store(new FileOutputStream(file), keystorePwd.toCharArray());
	    }
	    return keyStore;
	}
	
	public Object getRequestJson (String methodVerb)
    {
        if (getRequest () != null && getRequest().indexOf("{") >= 0)
        {
            try
            {
            	ObjectMapper mapper = new ObjectMapper();
        		if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_DISC_VERB))
        			return mapper.readValue(getRequest().substring (getRequest().indexOf("{")), DeviceDiscoveryRequestDetailDto.class);
        		
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
