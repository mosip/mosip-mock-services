package io.mosip.mock.sbi.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);	

	public static String base64UrlEncode (byte [] arg)
    {
		return Base64.getUrlEncoder().encodeToString(arg);
    }

	public static String base64UrlEncode (String arg)
    {
		return Base64.getUrlEncoder().encodeToString(arg.getBytes());
    }

    public static byte[] base64UrlDecode (String arg)
    {
    	return Base64.getUrlDecoder().decode(arg);    
	}
    
    public static byte [] toUtf8ByteArray (String arg)
    {
        return arg.getBytes (StandardCharsets.UTF_8);
    }
}
