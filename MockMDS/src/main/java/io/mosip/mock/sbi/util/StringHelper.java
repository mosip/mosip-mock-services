package io.mosip.mock.sbi.util;

import java.nio.charset.StandardCharsets;

import io.mosip.kernel.core.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);	

	public static String base64UrlEncode (byte [] arg)
    {
        return CryptoUtil.encodeToURLSafeBase64(arg);
    }

	public static String base64UrlEncode (String arg)
    {
        return CryptoUtil.encodeToURLSafeBase64(arg.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] base64UrlDecode (String arg)
    {
    	return CryptoUtil.decodeURLSafeBase64(arg);
	}

    public static byte [] toUtf8ByteArray (String arg)
    {
        return arg.getBytes (StandardCharsets.UTF_8);
    }
    
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if(value == null) {
            return false;
        }

        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
    
    public static boolean isAlphaNumericHyphenWithMinMaxLength(String input) {
        if(input == null) {
            return false;
        }
        // Regular expression for Match alphanumeric characters or hyphens, with no whitespaces and with a length between minLength and maxLength
        String regex = "^[a-zA-Z0-9-]{4,50}$";
        
        // Check if the input string matches the pattern
        return input.matches(regex);
    }
}