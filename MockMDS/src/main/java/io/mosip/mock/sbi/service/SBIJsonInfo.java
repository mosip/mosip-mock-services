package io.mosip.mock.sbi.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.registration.mdm.dto.BioMetricsDto;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.ErrorInfo;
import io.mosip.registration.mdm.dto.RCaptureResponse;
import lombok.Getter;
import lombok.Setter;

public class SBIJsonInfo {
	public static String getErrorJson (String lang, String errorCode, String exceptionMessage)
    {
		List<ErrorDto> errorList = new ArrayList<ErrorDto> ();
		StringBuilder sb = new StringBuilder ();
        ObjectMapper mapper = new ObjectMapper ();	
        
        ErrorDto errorDto = new ErrorDto ();
        ErrorInfo errorInfo = new ErrorInfo (errorCode, (getErrorDescription (lang, errorCode) + " " + exceptionMessage).trim());
        errorDto.error = errorInfo;
   		errorList.add(errorDto);
        try {
			return mapper.writeValueAsString(errorList);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
	public static String getAdminApiErrorJson (String lang, String errorCode, String exceptionMessage)
    {
        StringBuilder sb = new StringBuilder ();
        ObjectMapper mapper = new ObjectMapper ();	
        ErrorInfo errorInfo = new ErrorInfo (errorCode, (getErrorDescription (lang, errorCode) + " " + exceptionMessage).trim());
        try {
			return mapper.writeValueAsString(errorInfo);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

	public static String getStreamErrorJson (String lang, String errorCode, String exceptionMessage)
    {
        StringBuilder sb = new StringBuilder ();
        ObjectMapper mapper = new ObjectMapper ();	
        ErrorInfo errorInfo = new ErrorInfo (errorCode, (getErrorDescription (lang, errorCode) + " " + exceptionMessage).trim());
        try {
			return mapper.writeValueAsString(errorInfo);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

	public static String getCaptureErrorJson (String specVersion, String lang, String errorCode, String exceptionMessage, boolean isRCapture)
    {
        StringBuilder sb = new StringBuilder ();
        List<BioMetricsDto> biometrics = new ArrayList<BioMetricsDto> ();

        BioMetricsDto biometric = new BioMetricsDto ();
        biometric.setSpecVersion(specVersion);
        biometric.setData("");
        biometric.setHash("");
        if (isRCapture == false)
        {
            biometric.setSessionKey("");
            biometric.setThumbprint("");
        }

        biometric.setError(new ErrorInfo (errorCode, (getErrorDescription (lang, errorCode) + " " + exceptionMessage).trim()));

        RCaptureResponse captureResponse = new RCaptureResponse ();
        biometrics.add(biometric);
        captureResponse.setBiometrics(biometrics);
        
        ObjectMapper mapper = new ObjectMapper ();	
        SerializationConfig config = mapper.getSerializationConfig();
        config.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationConfig(config);

        try {
			return mapper.writeValueAsString(captureResponse);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
	public static String getErrorDescription (String lang, String errorCode)
    {
        if (lang == null || lang.trim ().equals (""))
            lang = "en";
        String message = "mds_ERROR_"+ errorCode + "_msg_"+ lang;
        String errorDescription = ApplicationPropertyHelper.getPropertyKeyValue (message);

        if (errorDescription == null || errorDescription.trim ().equals (""))
        {
        	errorDescription = "No Description available.";
        }
        return errorDescription;
    }	
}

@Getter
@Setter
class ErrorDto {
	public ErrorInfo error;
}
