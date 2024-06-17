package io.mosip.mock.sdk.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;

/**
 * Utility class providing various helper methods for cryptographic operations,
 * encoding and decoding data to/from URL-safe Base64, and checking for null or
 * empty values.
 * 
 * <p>
 * This class includes methods for:
 * <ul>
 * <li>Computing SHA-256 fingerprint of byte data combined with optional
 * metadata.</li>
 * <li>Comparing hash values of two byte arrays.</li>
 * <li>Encoding and decoding byte arrays or strings to/from URL-safe Base64
 * format.</li>
 * <li>Checking if a byte array or string is null or empty.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * All methods in this class handle {@link SDKException} when invalid input is
 * detected.
 * </p>
 * 
 */
public class Util {
	private Util() {
		throw new IllegalStateException("Util class");
	}

	/**
	 * Compares the SHA-256 hash values of two byte arrays.
	 *
	 * @param s1 First byte array.
	 * @param s2 Second byte array.
	 * @return {@code true} if the hash values match, otherwise {@code false}.
	 */
	public static boolean compareHash(byte[] s1, byte[] s2) {
		String checksum1 = computeFingerPrint(s1, null).toLowerCase();
		String checksum2 = computeFingerPrint(s2, null).toLowerCase();
		return checksum1.equals(checksum2);
	}

	/**
	 * Computes the SHA-256 hash (fingerprint) of a byte array combined with
	 * optional metadata.
	 *
	 * @param data     Byte array to compute hash from.
	 * @param metaData Optional metadata to include in hash computation.
	 * @return SHA-256 hash value as a hexadecimal string.
	 */
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

	/**
	 * Encodes a byte array to URL-safe Base64 string.
	 *
	 * @param data Byte array to encode.
	 * @return URL-safe Base64 encoded string.
	 * @throws SDKException If the input byte array is null or empty.
	 */
	public static String encodeToURLSafeBase64(byte[] data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());

		return urlSafeEncoder.encodeToString(data);
	}

	/**
	 * Encodes a string to URL-safe Base64 string.
	 *
	 * @param data String to encode.
	 * @return URL-safe Base64 encoded string.
	 * @throws SDKException If the input string is null or empty.
	 */
	public static String encodeToURLSafeBase64(String data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());

		return urlSafeEncoder.encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decodes a URL-safe Base64 encoded string to a byte array.
	 *
	 * @param data URL-safe Base64 encoded string to decode.
	 * @return Decoded byte array.
	 * @throws SDKException If the input string is null or empty.
	 */
	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());

		return Base64.getUrlDecoder().decode(data);
	}

	/**
	 * Decodes a URL-safe Base64 encoded byte array to a byte array.
	 *
	 * @param data URL-safe Base64 encoded byte array to decode.
	 * @return Decoded byte array.
	 * @throws SDKException If the input byte array is null or empty.
	 */
	public static byte[] decodeURLSafeBase64(byte[] data) {
		if (isNullEmpty(data))
			throw new SDKException(ResponseStatus.INVALID_INPUT.getStatusCode() + "",
					ResponseStatus.INVALID_INPUT.getStatusMessage());

		return Base64.getUrlDecoder().decode(data);
	}

	/**
	 * Checks if the given byte array is null or empty.
	 *
	 * @param array Byte array to check.
	 * @return {@code true} if the byte array is null or empty, otherwise
	 *         {@code false}.
	 */
	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Checks if the given string is null or empty.
	 *
	 * @param str String to check.
	 * @return {@code true} if the string is null or empty, otherwise {@code false}.
	 */
	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}