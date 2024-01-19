package io.mosip.mock.sdk.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

public class Util {
	public static boolean compareHash(byte[] s1, byte[] s2) throws NoSuchAlgorithmException {
		String checksum1 = computeFingerPrint(s1, null).toLowerCase();
		String checksum2 = computeFingerPrint(s2, null).toLowerCase();
		return checksum1.equals(checksum2);
	}

	public static String computeFingerPrint(byte[] data, String metaData) throws NoSuchAlgorithmException {
		byte[] combinedPlainTextBytes = null;
		if (metaData == null) {
			combinedPlainTextBytes = ArrayUtils.addAll(data);
		} else {
			combinedPlainTextBytes = ArrayUtils.addAll(data, metaData.getBytes());
		}
		return DigestUtils.sha256Hex(combinedPlainTextBytes);
	}

	private static Encoder urlSafeEncoder;
	static {
		urlSafeEncoder = Base64.getUrlEncoder().withoutPadding();
	}

	public static String encodeToURLSafeBase64(byte[] data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data);
	}

	public static String encodeToURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return Base64.getUrlDecoder().decode(data);
	}

	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

}
