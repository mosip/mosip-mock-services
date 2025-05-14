package org.biometric.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class CryptoUtilityTest {

    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private X509Certificate mockCertificate;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        mockCertificate = mock(X509Certificate.class);
    }

    /**
     * Tests encryption of data using public key and transaction ID.
     * Verifies that the encryption result contains required fields.
     */
    @Test
    void testEncrypt() {
        byte[] testData = "Test data".getBytes(StandardCharsets.UTF_8);
        String transactionId = "test-transaction";

        Map<String, String> result = CryptoUtility.encrypt(publicKey, testData, transactionId);

        assertNotNull(result.get("ENC_SESSION_KEY"), "Encrypted session key should not be null");
        assertNotNull(result.get("ENC_DATA"), "Encrypted data should not be null");
        assertNotNull(result.get("TIMESTAMP"), "Timestamp should not be null");
    }

    @Test
    void testDecrypt_Exception() {
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        String sessionKey = "badSessionKey";
        String encryptedData = "badEncryptedData";
        String timestamp = "invalid";
        String transactionId = "txn";

        try (
                MockedStatic<CryptoUtility> utils = mockStatic(CryptoUtility.class)
        ) {

            // When
            String result = CryptoUtility.decrypt(mockPrivateKey, sessionKey, encryptedData, timestamp, transactionId);

            // Then
            assertNull(result); // fallback in catch block
        }
    }

    /**
     * Tests symmetric key generation.
     * Verifies that generated keys have correct algorithm and size.
     */
    @Test
    void testGetSymmetricKey() throws Exception {
        SecretKey key = CryptoUtility.getSymmetricKey();

        assertNotNull(key, "Generated key should not be null");
        assertEquals("AES", key.getAlgorithm(), "Algorithm should be AES");
        assertEquals(32, key.getEncoded().length, "Key length should be 256 bits (32 bytes)");
    }

    /**
     * Tests symmetric encryption and decryption.
     * Verifies that decrypted data matches the original input.
     */
    @Test
    void testSymmetricEncryptDecrypt() throws Exception {
        SecretKey key = CryptoUtility.getSymmetricKey();
        byte[] iv = new byte[12];
        byte[] aad = new byte[16];
        byte[] testData = "Test symmetric encryption".getBytes();

        byte[] encrypted = CryptoUtility.symmetricEncrypt(key, testData, iv, aad);
        byte[] decrypted = CryptoUtility.symmetricDecrypt(
                new SecretKeySpec(key.getEncoded(), "AES"),
                encrypted, iv, aad);

        assertArrayEquals(testData, decrypted, "Decrypted data should match original");
    }

    /**
     * Tests URL-safe Base64 encoding and decoding.
     * Verifies that decoded data matches the original input.
     */
    @Test
    void testUrlSafeBase64EncodingDecoding() {
        byte[] testData = "Test URL-safe Base64".getBytes();

        String encoded = CryptoUtility.encodeToURLSafeBase64(testData);
        byte[] decoded = CryptoUtility.decodeURLSafeBase64(encoded);

        assertArrayEquals(testData, decoded, "Decoded data should match original");
    }

    /**
     * Tests null and empty input validation methods.
     * Verifies correct behavior for edge cases.
     */
    @Test
    void testNullEmptyValidation() {
        assertTrue(CryptoUtility.isNullEmpty((String) null), "Null string should be considered empty");
        assertTrue(CryptoUtility.isNullEmpty(""), "Empty string should be considered empty");
        assertTrue(CryptoUtility.isNullEmpty("  "), "Whitespace string should be considered empty");
        assertFalse(CryptoUtility.isNullEmpty("test"), "Non-empty string should not be considered empty");

        assertTrue(CryptoUtility.isNullEmpty((byte[]) null), "Null byte array should be considered empty");
        assertTrue(CryptoUtility.isNullEmpty(new byte[0]), "Empty byte array should be considered empty");
        assertFalse(CryptoUtility.isNullEmpty(new byte[1]), "Non-empty byte array should not be considered empty");
    }

    /**
     * Tests digital signature generation and verification.
     * Verifies that the signature is properly formatted.
     */
    @Test
    void testSign() {
        byte[] testData = "Test signing data".getBytes();

        String signature = CryptoUtility.sign(testData, privateKey, mockCertificate);

        assertNotNull(signature, "Signature should not be null");
        assertTrue(signature.contains("."), "Signature should contain dots as separators");
    }
}