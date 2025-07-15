package io.mosip.mock.sbi.util;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StringHelperTest {

    /**
     * Tests base64UrlEncode method with byte array input.
     * Verifies that byte array is correctly encoded to URL-safe base64 string.
     */
    @Test
    void base64UrlEncode_byteArray_success() {
        byte[] input = "Test String".getBytes(StandardCharsets.UTF_8);
        String encoded = StringHelper.base64UrlEncode(input);
        assertNotNull(encoded, "Encoded string should not be null");
        assertTrue(encoded.matches("^[A-Za-z0-9_-]*$"), "Should be URL safe base64");
    }

    /**
     * Tests base64UrlEncode method with String input.
     * Verifies that string is correctly encoded to URL-safe base64 string.
     */
    @Test
    void base64UrlEncode_string_success() {
        String input = "Test String";
        String encoded = StringHelper.base64UrlEncode(input);
        assertNotNull(encoded, "Encoded string should not be null");
        assertTrue(encoded.matches("^[A-Za-z0-9_-]*$"), "Should be URL safe base64");
    }

    /**
     * Tests base64UrlDecode method.
     * Verifies that base64 encoded string is correctly decoded back to original bytes.
     */
    @Test
    void base64UrlDecode_success() {
        String original = "Test String";
        String encoded = StringHelper.base64UrlEncode(original);
        byte[] decoded = StringHelper.base64UrlDecode(encoded);
        String result = new String(decoded, StandardCharsets.UTF_8);
        assertEquals(original, result, "Decoded string should match original");
    }

    /**
     * Tests toUtf8ByteArray method.
     * Verifies that string is correctly converted to UTF-8 byte array.
     */
    @Test
    void toUtf8ByteArray_success() {
        String input = "Test String";
        byte[] result = StringHelper.toUtf8ByteArray(input);
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), result,
                "Byte array should match UTF-8 encoding");
    }

    /**
     * Tests isValidLength method with various inputs.
     * Verifies length validation works correctly for different scenarios.
     */
    @Test
    void isValidLength_variousInputs_success() {
        assertTrue(StringHelper.isValidLength("test", 2, 6),
                "String length within range should return true");
        assertFalse(StringHelper.isValidLength("test", 5, 10),
                "String length below min should return false");
        assertFalse(StringHelper.isValidLength("test", 1, 3),
                "String length above max should return false");
        assertFalse(StringHelper.isValidLength(null, 1, 5),
                "Null string should return false");
    }

    /**
     * Tests isAlphaNumericHyphenWithMinMaxLength method with valid inputs.
     * Verifies that valid alphanumeric strings with hyphens are accepted.
     */
    @Test
    void isAlphaNumericHyphenWithMinMaxLength_validInputs_success() {
        assertTrue(StringHelper.isAlphaNumericHyphenWithMinMaxLength("a1b2-c3d4"),
                "Valid alphanumeric with hyphen should return true");
        assertTrue(StringHelper.isAlphaNumericHyphenWithMinMaxLength("abcd"),
                "Valid minimum length string should return true");
        assertTrue(StringHelper.isAlphaNumericHyphenWithMinMaxLength("abcd-1234-efgh"),
                "Valid longer string should return true");
    }

    /**
     * Tests isAlphaNumericHyphenWithMinMaxLength method with invalid inputs.
     * Verifies that invalid strings are rejected.
     */
    @Test
    void isAlphaNumericHyphenWithMinMaxLength_invalidInputs_failure() {
        assertFalse(StringHelper.isAlphaNumericHyphenWithMinMaxLength("abc"),
                "String below min length should return false");
        assertFalse(StringHelper.isAlphaNumericHyphenWithMinMaxLength("a b"),
                "String with space should return false");
        assertFalse(StringHelper.isAlphaNumericHyphenWithMinMaxLength("abc@123"),
                "String with special characters should return false");
        assertFalse(StringHelper.isAlphaNumericHyphenWithMinMaxLength(null),
                "Null string should return false");
        assertFalse(StringHelper.isAlphaNumericHyphenWithMinMaxLength("a".repeat(51)),
                "String above max length should return false");
    }
}
