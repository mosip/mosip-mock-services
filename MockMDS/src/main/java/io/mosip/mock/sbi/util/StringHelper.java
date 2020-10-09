package io.mosip.mock.sbi.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);	
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

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
    
    public static String toHexaFromByteArray (byte [] data)
    {
    	 byte[] hexChars = new byte[data.length * 2];
	    for (int j = 0; j < data.length; j++) {
	        int v = data[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars, StandardCharsets.UTF_8);
    }
}
