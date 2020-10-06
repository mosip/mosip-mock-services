package io.mosip.mock.sbi.service;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class SBIResponseInfo {

	public static String generateErrorResponse (String lang, int port, String errorCode, String exceptionMessage)
	{
		String httpResponse = "";
		String responseJSON = SBIJsonInfo.getErrorJson (lang, errorCode, exceptionMessage);
		httpResponse = "HTTP/1.1 405 OK\r\n";
		httpResponse += getAccess_Control_Allow_Info ();
		httpResponse += "CACHE-CONTROL:no-cache\r\n";
		if (responseJSON != null)
		{
			httpResponse += "Content-Length: " + responseJSON.length () + "\r\n";
		}
		httpResponse += "Content-Type: application/json\r\n";
		httpResponse += "LOCATION: HTTP://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + port + "/\r\n";
		httpResponse += "Connection: close\r\n\r\n";
		httpResponse += responseJSON + "\r\n\r\n";
		
		return httpResponse;
	}
	
	public static String generateResponse (String lang, int port, String response)
	{
		String httpResponse = "";
		httpResponse = "HTTP/1.1 200 OK\r\n";
		httpResponse += getAccess_Control_Allow_Info ();
		httpResponse += "CACHE-CONTROL:no-cache\r\n";
		if (response != null)
		{
			httpResponse += "Content-Length: " + response.length () + "\r\n";
		}
		httpResponse += "Content-Type: application/json\r\n";
		httpResponse += "LOCATION: HTTP://" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + port + "/\r\n";
		httpResponse += "Connection: close\r\n\r\n";
		httpResponse += response + "\r\n\r\n";

		return httpResponse;
	}
	
	private static String getAccess_Control_Allow_Info ()
	{
		String httpHeaderInfo = "";
		String allow_Origin = getAccess_Control_Allow_Origin ();
		httpHeaderInfo += getAccess_Control_Allow_Headers ();
		httpHeaderInfo += allow_Origin;
		httpHeaderInfo += getAccess_Control_Allow_Methods ();
		httpHeaderInfo += getAccess_Control_Allow_Credentials ();
		return httpHeaderInfo;
	}

	private static String getAccess_Control_Allow_Origin ()
    {
		String accessOrigin = "Access-Control-Allow-Origin: *\r\n";
		return accessOrigin;
    }
	private static String getAccess_Control_Allow_Headers ()
	{
		String accessHeaders = "Access-Control-Allow-Headers:DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,X-PINGOTHER,Authorization\r\n";
		return accessHeaders;
	}

	private static String getAccess_Control_Allow_Credentials ()
	{
		String accessHeaders = "Access-Control-Allow-Credentials: true\r\n";
		return accessHeaders;
	}

	private static String getAccess_Control_Allow_Methods ()
	{
		String accessHeaders = "Access-Control-Allow-Methods: " + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS) + "\r\n";
		return accessHeaders;
	}
}
