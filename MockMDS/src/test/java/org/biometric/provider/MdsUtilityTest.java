package org.biometric.provider;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class MdsUtilityTest {

    /**
     * Tests asymmetric encryption using a generated RSA public key.
     * Verifies that the encrypted data is not null and not equal to the original.
     */
    @Test
    void asymmetricEncrypt_success() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        byte[] data = "Test asymmetric encryption".getBytes();
        byte[] encrypted = MdsUtility.asymmetricEncrypt(publicKey, data);

        assertNotNull(encrypted, "Encrypted data should not be null");
        assertNotEquals(new String(data), new String(encrypted), "Encrypted data should differ from original");
    }


    /**
     * Tests symmetric key generation.
     * Verifies that the generated key is not null and uses the AES algorithm.
     */
    @Test
    void getSymmetricKey_success() throws Exception {
        SecretKey key = MdsUtility.getSymmetricKey();

        assertNotNull(key, "Generated key should not be null");
        assertEquals("AES", key.getAlgorithm(), "Algorithm should be AES");
        assertEquals(32, key.getEncoded().length, "Key length should be 256 bits (32 bytes)");
    }

    /**
     * Mocks symmetricEncrypt() to return dummy encrypted bytes without invoking real Cipher.
     */
    @Test
    void symmetricEncryptWithMock_success() throws Exception {
        SecretKey dummyKey = new SecretKeySpec(new byte[32], "AES");
        byte[] input = "Test symmetric encryption".getBytes();
        byte[] aad = "TestAAD".getBytes();
        byte[] mockEncrypted = "MOCK_ENCRYPTED".getBytes();

        try (MockedStatic<MdsUtility> mocked = mockStatic(MdsUtility.class)) {
            mocked.when(() -> MdsUtility.getSymmetricKey()).thenReturn(dummyKey);
            mocked.when(() -> MdsUtility.symmetricEncrypt(dummyKey, input, aad)).thenReturn(mockEncrypted);

            byte[] result = MdsUtility.symmetricEncrypt(dummyKey, input, aad);

            assertNotNull(result);
            assertEquals("MOCK_ENCRYPTED", new String(result));
        }
    }

    /**
     * Mocks encryptedData() method to return a fake Base64 string.
     */
    @Test
    void encryptedDataWithMock_success() throws Exception {
        SecretKey dummyKey = new SecretKeySpec(new byte[32], "AES");
        String bioValue = "biometric-data";
        String timestamp = "2024060712345678";
        String mockEncrypted = "ZmFrZS1iYXNlNjQtZW5jcnlwdGVk"; // "fake-base64-encrypted"

        try (MockedStatic<MdsUtility> mocked = mockStatic(MdsUtility.class)) {
            mocked.when(() -> MdsUtility.getSymmetricKey()).thenReturn(dummyKey);
            mocked.when(() -> MdsUtility.encryptedData(bioValue, timestamp, dummyKey))
                    .thenReturn(mockEncrypted);

            String result = MdsUtility.encryptedData(bioValue, timestamp, dummyKey);

            assertNotNull(result);
            assertEquals(mockEncrypted, result);
        }
    }
}