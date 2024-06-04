package io.mosip.mock.sdk.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;

public class Util {
	private Util() {
		throw new IllegalStateException("Util class");
	}

	public static boolean compareHash(byte[] s1, byte[] s2) {
		String checksum1 = computeFingerPrint(s1, null).toLowerCase();
		String checksum2 = computeFingerPrint(s2, null).toLowerCase();
		return checksum1.equals(checksum2);
	}

	public static String computeFingerPrint(byte[] data, String metaData) {
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
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());
		
		return urlSafeEncoder.encodeToString(data);
	}

	public static String encodeToURLSafeBase64(String data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());
		
		return urlSafeEncoder.encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());
		
		return Base64.getUrlDecoder().decode(data);
	}

	public static byte[] decodeURLSafeBase64(byte[] data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());

		return Base64.getUrlDecoder().decode(data);
	}

	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}