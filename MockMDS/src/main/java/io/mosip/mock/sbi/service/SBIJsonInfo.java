package io.mosip.mock.sbi.service;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.registration.mdm.dto.ErrorInfo;

public class SBIJsonInfo {

	public static String getErrorJson (String lang, String errorCode, String exceptionMessage)
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
