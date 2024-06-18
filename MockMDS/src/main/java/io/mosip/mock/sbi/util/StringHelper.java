package io.mosip.mock.sbi.util;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.mosip.kernel.core.util.CryptoUtil;

public class StringHelper {
	private StringHelper() {
		throw new IllegalStateException("StringHelper class");
	}

	public static String base64UrlEncode(byte[] arg) {
		return CryptoUtil.encodeToURLSafeBase64(arg);
	}

	public static String base64UrlEncode(String arg) {
		return CryptoUtil.encodeToURLSafeBase64(arg.getBytes(StandardCharsets.UTF_8));
	}

	public static byte[] base64UrlDecode(String arg) {
		return CryptoUtil.decodeURLSafeBase64(arg);
	}

	public static byte[] toUtf8ByteArray(String arg) {
		return arg.getBytes(StandardCharsets.UTF_8);
	}

	public static boolean isValidLength(String value, int minLength, int maxLength) {
		if (Objects.isNull(value)) {
			return false;
		}

		int length = value.length();
		return length >= minLength && length <= maxLength;
	}

	public static boolean isAlphaNumericHyphenWithMinMaxLength(String input) {
		if (Objects.isNull(input)) {
			return false;
		}
		// Regular expression for Match alphanumeric characters or hyphens, with no
		// whitespaces and with a length between minLength and maxLength
		String regex = "^[a-zA-Z0-9-]{4,50}$";

		// Check if the input string matches the pattern
		return input.matches(regex);
	}
}