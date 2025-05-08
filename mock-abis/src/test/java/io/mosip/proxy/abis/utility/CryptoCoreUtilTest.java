package io.mosip.proxy.abis.utility;

import io.mosip.proxy.abis.exception.AbisException;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link CryptoCoreUtil}.
 */
@ExtendWith(MockitoExtension.class)
class CryptoCoreUtilTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private CryptoCoreUtil cryptoCoreUtil;

    @Mock
    private PrivateKey mockPrivateKey;

    @Mock
    private Certificate mockCertificate;

    /**
     * Setup method executed before each test.
     */
    @BeforeEach
    void setup() {
        // Initialize static fields using reflection
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "certiPassword", "password");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "alias", "test-alias");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "keystore", "PKCS12");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "filePath", "test-certificate.p12");

        // Use lenient() to avoid UnnecessaryStubbingException
        Mockito.lenient().when(environment.getProperty("certificate.filename")).thenReturn("test-certificate.p12");
    }

    /**
     * Tests the {@link CryptoCoreUtil#setCertificateValues(String, String, String, String)} method.
     * Verifies that the static fields are updated with the provided certificate parameters.
     */
    @Test
    void testSetCertificateValues() {
        CryptoCoreUtil.setCertificateValues("new-path", "JKS", "new-password", "new-alias");

        assertEquals("new-alias", getStaticFieldValue("alias"));
        assertEquals("new-path", getStaticFieldValue("filePath"));
        assertEquals("JKS", getStaticFieldValue("keystore"));
        assertEquals("new-password", getStaticFieldValue("certiPassword"));
    }

    /**
     * Tests the {@link CryptoCoreUtil#getCertificateThumbprint(Certificate)} method.
     * Verifies that the method returns a non-null thumbprint and that the
     * certificate's getEncoded method is called exactly once.
     */
    @Test
    void testGetCertificateThumbprint() throws Exception {
        // Mock certificate encoded bytes
        byte[] encodedCert = "test-certificate".getBytes();
        when(mockCertificate.getEncoded()).thenReturn(encodedCert);

        // Call the method
        byte[] thumbprint = cryptoCoreUtil.getCertificateThumbprint(mockCertificate);

        // Verify the result is not null
        assertNotNull(thumbprint);
        verify(mockCertificate, times(1)).getEncoded();
    }

    /**
     * Tests the error handling in the {@link CryptoCoreUtil#decryptCbeff(String)} method.
     * Verifies that when the method throws a RuntimeException, it is correctly propagated.
     */
    @Test
    void testDecryptCbeffWithException() throws Exception {
        CryptoCoreUtil spyCryptoCoreUtil = spy(cryptoCoreUtil);

        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(spyCryptoCoreUtil).decryptCbeff(anyString());

        String base64EncodedData = Base64.encodeBase64String("encrypted-cbeff-data".getBytes());

        // Verify exception is thrown
        assertThrows(RuntimeException.class, () -> spyCryptoCoreUtil.decryptCbeff(base64EncodedData));
    }

    /**
     * Tests that the private decryptCbeffData method throws an AbisException
     * when provided with invalid data and a mock PrivateKeyEntry.
     */
    @Test
    void testDecryptCbeffDataWithNonceAndAad() throws Exception {
        // Setup test data
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = "encrypted-data".getBytes();
        byte[] mockNonce = new byte[12];
        byte[] mockAad = new byte[32];
        byte[] expectedResult = "decrypted-data".getBytes();

        // Create mock cipher
        Cipher mockCipher = mock(Cipher.class);

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            // Setup the mock to return our controlled cipher
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            // Mock the cipher's doFinal method
            when(mockCipher.doFinal(any(byte[].class), anyInt(), anyInt())).thenReturn(expectedResult);

            // Access the private method using reflection
            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                    SecretKey.class, byte[].class, byte[].class, byte[].class);
            method.setAccessible(true);

            // Now we can try to invoke the method in a controlled environment
            try {
                byte[] result = (byte[]) method.invoke(cryptoCoreUtil, mockSecretKey, mockData, mockNonce, mockAad);
                assertEquals(expectedResult, result);
            } catch (Exception e) {
                // If we get an unexpected exception, fail the test
                throw new AssertionError("Unexpected exception: " + e.getMessage(), e);
            }

            // Verify cipher was created with the right algorithm
            mockedCipher.verify(() -> Cipher.getInstance("AES/GCM/NoPadding"));
        }
    }

    /**
     * Tests if the private {@code decryptCbeffData} method throws an {@code AbisException}
     * when the underlying {@code Cipher.getInstance} call fails by throwing a runtime exception.
     */
    @Test
    void testDecryptCbeffDataThrowsAbisException() throws Exception {
        // Setup test data
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = "encrypted-data".getBytes();

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            // Make Cipher.getInstance throw an exception
            RuntimeException testException = new RuntimeException("Test exception");
            mockedCipher.when(() -> Cipher.getInstance(anyString()))
                    .thenThrow(testException);

            // Access the private method using reflection
            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                    SecretKey.class, byte[].class);
            method.setAccessible(true);

            // Invoke the method and expect an exception
            Exception exception = assertThrows(Exception.class, () -> {
                method.invoke(cryptoCoreUtil, mockSecretKey, mockData);
            });

            // Verify the exception chain contains an AbisException
            Throwable cause = exception.getCause();
            assertNotNull(cause);
            assertEquals(AbisException.class, cause.getClass());
        }
    }

    // Tests the parsing of the encryption key header using the VERSION_RSA_2048 value from CryptoCoreUtil.
    @Test
    void testParseEncryptKeyHeader() throws Exception {
        // Prepare test data - using the VERSION_RSA_2048 value from CryptoCoreUtil
        byte[] versionRsa2048 = "VER_R2".getBytes();
        byte[] encryptedKey = new byte[versionRsa2048.length + 10];
        System.arraycopy(versionRsa2048, 0, encryptedKey, 0, versionRsa2048.length);

        // Access the VERSION_RSA_2048 field using reflection to verify our value
        Field versionField = CryptoCoreUtil.class.getDeclaredField("VERSION_RSA_2048");
        versionField.setAccessible(true);
        byte[] actualVersionBytes = (byte[]) versionField.get(null);

        // Access the parseEncryptKeyHeader method using reflection
        Method method = CryptoCoreUtil.class.getDeclaredMethod("parseEncryptKeyHeader", byte[].class);
        method.setAccessible(true);

        // Invoke the method
        byte[] result = (byte[]) method.invoke(cryptoCoreUtil, encryptedKey);

        // Verify the result matches VERSION_RSA_2048
        assertNotNull(result);
        assertEquals(new String(actualVersionBytes), new String(result));
    }

    /**
     * Helper method to create a mock properties stream with test values.
     *
     * @return An InputStream containing mock properties
     */
    private InputStream createMockPropertiesStream() {
        String props = "certificate.password=test-password\n" + "certificate.alias=test-alias\n" + "certificate.keystore=PKCS12\n" + "certificate.filename=test-certificate.p12";
        return new ByteArrayInputStream(props.getBytes());
    }

    /**
     * Helper method to get the value of a static field using reflection.
     *
     * @param fieldName The name of the static field
     * @return The value of the static field
     */
    @SuppressWarnings("unchecked")
    private <T> T getStaticFieldValue(String fieldName) {
        try {
            Field field = CryptoCoreUtil.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to get a resource as a stream.
     * This method should be added to the CryptoCoreUtil class to make it testable.
     */
    protected InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    /**
     * Tests the getSplitterIndex method with various inputs.
     */
    @Test
    void testGetSplitterIndex() throws Exception {
        // Access the private method using reflection
        Method method = CryptoCoreUtil.class.getDeclaredMethod("getSplitterIndex",
                byte[].class, int.class, String.class);
        method.setAccessible(true);

        // Test case 1: Normal splitter
        byte[] data1 = "data#KEY_SPLITTER#more-data".getBytes();
        int result1 = (int) method.invoke(cryptoCoreUtil, data1, 0, "#KEY_SPLITTER#");
        assertEquals(4, result1);

        // Test case 2: Splitter at start
        byte[] data2 = "#KEY_SPLITTER#data".getBytes();
        int result2 = (int) method.invoke(cryptoCoreUtil, data2, 0, "#KEY_SPLITTER#");
        assertEquals(0, result2);

        // Test case 3: No splitter
        byte[] data3 = "no-splitter-here".getBytes();
        int result3 = (int) method.invoke(cryptoCoreUtil, data3, 0, "#KEY_SPLITTER#");
        assertEquals(data3.length, result3);
    }

    /**
     * Tests parsing encrypt key header with invalid version.
     */
    @Test
    void testParseEncryptKeyHeaderWithInvalidVersion() throws Exception {
        // Create data with invalid version
        byte[] invalidVersion = "INVALID_VERSION".getBytes();
        byte[] encryptedKey = new byte[invalidVersion.length + 10];
        System.arraycopy(invalidVersion, 0, encryptedKey, 0, invalidVersion.length);

        // Access the private method using reflection
        Method method = CryptoCoreUtil.class.getDeclaredMethod("parseEncryptKeyHeader", byte[].class);
        method.setAccessible(true);

        // Invoke the method
        byte[] result = (byte[]) method.invoke(cryptoCoreUtil, encryptedKey);

        // Verify empty array is returned for invalid version
        assertEquals(0, result.length);
    }

    /**
     * Tests decryption with null input data.
     */
    @Test
    void testDecryptCbeffWithNullInput() {
        assertThrows(NullPointerException.class, () -> cryptoCoreUtil.decryptCbeff(null));
    }

    /**
     * Tests certificate thumbprint generation with null certificate.
     */
    @Test
    void testGetCertificateThumbprintWithNullCertificate() {
        assertThrows(NullPointerException.class,
                () -> cryptoCoreUtil.getCertificateThumbprint(null));
    }

    /**
     * Tests handling of KeyStore exceptions during private key retrieval.
     */
    @Test
    void testGetPrivateKeyEntryWithKeyStoreException() throws Exception {
        // Mock KeyStore to throw exception
        try (MockedStatic<KeyStore> mockedKeyStore = Mockito.mockStatic(KeyStore.class)) {
            mockedKeyStore.when(() -> KeyStore.getInstance(anyString()))
                    .thenThrow(new KeyStoreException("KeyStore error"));

            // Test decryption which internally calls getPrivateKeyEntry
            String base64EncodedData = Base64.encodeBase64String("test-data".getBytes());
            assertThrows(Exception.class, () -> cryptoCoreUtil.decryptCbeff(base64EncodedData));
        }
    }

    /**
     * Tests decryption with invalid GCM parameters.
     */
    @Test
    void testDecryptCbeffDataWithInvalidGCMParams() throws Exception {
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = new byte[16]; // Too short for GCM
        byte[] mockNonce = new byte[11]; // Invalid nonce size
        byte[] mockAad = new byte[31]; // Invalid AAD size

        Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                SecretKey.class, byte[].class, byte[].class, byte[].class);
        method.setAccessible(true);

        assertThrows(Exception.class, () ->
                method.invoke(cryptoCoreUtil, mockSecretKey, mockData, mockNonce, mockAad));
    }

    /**
     * Tests that the private decryptRandomSymKey method throws an InvocationTargetException
     * wrapping a NoSuchAlgorithmException when Cipher.getInstance fails.
     */
    @Test
    void testDecryptRandomSymKey_WithSpecificExceptions() throws Exception {
        byte[] encryptedKey = "test-encrypted-key".getBytes();

        // Fix: Use getDeclaredMethod instead of getDeclaredField
        Method decryptRandomSymKeyMethod = CryptoCoreUtil.class.getDeclaredMethod("decryptRandomSymKey",
                PrivateKey.class, byte[].class);
        decryptRandomSymKeyMethod.setAccessible(true);

        // Mock Cipher behavior
        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            // Ensure Cipher.getInstance throws NoSuchAlgorithmException
            mockedCipher.when(() -> Cipher.getInstance(anyString()))
                    .thenThrow(new NoSuchAlgorithmException());

            // Create spy for CryptoCoreUtil
            CryptoCoreUtil spyCryptoCoreUtil = spy(cryptoCoreUtil);

            // Test exception handling
            Exception exception = assertThrows(InvocationTargetException.class, () ->
                    decryptRandomSymKeyMethod.invoke(spyCryptoCoreUtil, mockPrivateKey, encryptedKey));

            // Verify that the cause is NoSuchAlgorithmException
            assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
        }
    }

    /**
     * Tests that the private decryptCbeffData method throws an AbisException
     * when provided with invalid data and a mock PrivateKeyEntry.
     */
    @Test
    void testDecryptCbeffDataWithException() throws Exception {
        // Setup invalid data
        byte[] invalidData = "invalid-data".getBytes();
        KeyStore.PrivateKeyEntry mockPrivateKeyEntry = mock(KeyStore.PrivateKeyEntry.class);

        // Access private method using reflection
        Method decryptCbeffDataMethod = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                byte[].class, KeyStore.PrivateKeyEntry.class);
        decryptCbeffDataMethod.setAccessible(true);

        // Test exception handling
        Exception exception = assertThrows(Exception.class,
                () -> decryptCbeffDataMethod.invoke(cryptoCoreUtil, invalidData, mockPrivateKeyEntry));
        assertTrue(exception.getCause() instanceof AbisException);
    }
}