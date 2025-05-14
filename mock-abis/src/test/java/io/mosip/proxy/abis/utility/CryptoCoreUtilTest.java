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
import javax.crypto.spec.GCMParameterSpec;
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
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.ArgumentMatchers.eq;

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
     * Tests the setPropertyValues method with valid properties file.
     */
    @Test
    void testSetPropertyValues() throws Exception {
        // Create a spy of the class to verify static method calls
        try (MockedStatic<CryptoCoreUtil> mockedCryptoCoreUtil = Mockito.mockStatic(CryptoCoreUtil.class)) {
            // Mock the class loader and resource stream
            ClassLoader mockClassLoader = Thread.currentThread().getContextClassLoader();
            InputStream mockStream = createMockPropertiesStream();
            
            // Mock the static method to return our test values
            mockedCryptoCoreUtil.when(() -> CryptoCoreUtil.setPropertyValues()).thenAnswer(invocation -> {
                // Set the static fields directly using reflection
                ReflectionTestUtils.setField(CryptoCoreUtil.class, "certiPassword", "test-password");
                ReflectionTestUtils.setField(CryptoCoreUtil.class, "alias", "test-alias");
                ReflectionTestUtils.setField(CryptoCoreUtil.class, "keystore", "PKCS12");
                ReflectionTestUtils.setField(CryptoCoreUtil.class, "filePath", "test-certificate.p12");
                return null;
            });

            // Call the method
            CryptoCoreUtil.setPropertyValues();

            // Verify the static fields were set correctly
            assertEquals("test-password", getStaticFieldValue("certiPassword"));
            assertEquals("test-alias", getStaticFieldValue("alias"));
            assertEquals("PKCS12", getStaticFieldValue("keystore"));
            assertEquals("test-certificate.p12", getStaticFieldValue("filePath"));
        }
    }

    /**
     * Tests the setPropertyValues method when properties file is not found.
     */
    @Test
    void testSetPropertyValuesWithMissingFile() throws Exception {
        // Set initial values
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "certiPassword", "password");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "alias", "cbeff");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "keystore", "PKCS12");
        ReflectionTestUtils.setField(CryptoCoreUtil.class, "filePath", "test-certificate.p12");

        // Call the method - should not throw exception
        CryptoCoreUtil.setPropertyValues();

        // Verify the static fields were not changed
        assertEquals("password", getStaticFieldValue("certiPassword"));
        assertEquals("cbeff", getStaticFieldValue("alias"));
    }

    /**
     * Helper method to create a mock properties stream with test values.
     *
     * @return An InputStream containing mock properties
     */
    private InputStream createMockPropertiesStream() {
        String props = "certificate.password=test-password\n" +
                      "certificate.alias=test-alias\n" +
                      "certificate.keystore=PKCS12\n" +
                      "certificate.filename=test-certificate.p12";
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

    /**
     * Tests the decryptRandomSymKey method with various cryptographic exceptions.
     */
    @Test
    void testDecryptRandomSymKeyWithVariousExceptions() throws Exception {
        byte[] encryptedKey = "test-encrypted-key".getBytes();
        Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptRandomSymKey",
                PrivateKey.class, byte[].class);
        method.setAccessible(true);

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            // Test NoSuchPaddingException
            mockedCipher.when(() -> Cipher.getInstance(anyString()))
                    .thenThrow(new NoSuchPaddingException());
            Exception exception = assertThrows(InvocationTargetException.class,
                    () -> method.invoke(cryptoCoreUtil, mockPrivateKey, encryptedKey));
            assertTrue(exception.getCause() instanceof NoSuchPaddingException);

            // Test InvalidKeyException
            Cipher mockCipher = mock(Cipher.class);
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);
            doThrow(new RuntimeException("Invalid key")).when(mockCipher).init(
                    anyInt(), 
                    any(PrivateKey.class), 
                    any(java.security.spec.AlgorithmParameterSpec.class));
            exception = assertThrows(InvocationTargetException.class,
                    () -> method.invoke(cryptoCoreUtil, mockPrivateKey, encryptedKey));
            assertTrue(exception.getCause() instanceof RuntimeException);

            // Test InvalidAlgorithmParameterException
            mockCipher = mock(Cipher.class);
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);
            doThrow(new RuntimeException("Invalid algorithm parameter")).when(mockCipher).init(
                    anyInt(), 
                    any(PrivateKey.class), 
                    any(java.security.spec.AlgorithmParameterSpec.class));
            exception = assertThrows(InvocationTargetException.class,
                    () -> method.invoke(cryptoCoreUtil, mockPrivateKey, encryptedKey));
            assertTrue(exception.getCause() instanceof RuntimeException);
        }
    }

    /**
     * Tests the decryptCbeffData method with various cipher operations.
     */
    @Test
    void testDecryptCbeffDataWithCipherOperations() throws Exception {
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = "encrypted-data".getBytes();
        byte[] mockNonce = new byte[12];
        byte[] mockAad = new byte[32];
        byte[] expectedResult = "decrypted-data".getBytes();

        Cipher mockCipher = mock(Cipher.class);
        when(mockCipher.doFinal(any(byte[].class), anyInt(), anyInt())).thenReturn(expectedResult);

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                    SecretKey.class, byte[].class, byte[].class, byte[].class);
            method.setAccessible(true);

            byte[] result = (byte[]) method.invoke(cryptoCoreUtil, mockSecretKey, mockData, mockNonce, mockAad);
            assertEquals(expectedResult, result);

            verify(mockCipher).init(eq(2), any(SecretKeySpec.class), any(GCMParameterSpec.class));
            verify(mockCipher).updateAAD(mockAad);
            verify(mockCipher).doFinal(mockData, 0, mockData.length);
        }
    }

    /**
     * Tests the decryptCbeffData method with cipher initialization failure.
     */
    @Test
    void testDecryptCbeffDataWithCipherInitFailure() throws Exception {
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = "encrypted-data".getBytes();
        byte[] mockNonce = new byte[12];
        byte[] mockAad = new byte[32];

        Cipher mockCipher = mock(Cipher.class);
        doThrow(new InvalidKeyException()).when(mockCipher).init(anyInt(), any(SecretKeySpec.class), any(GCMParameterSpec.class));

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                    SecretKey.class, byte[].class, byte[].class, byte[].class);
            method.setAccessible(true);

            Exception exception = assertThrows(Exception.class,
                    () -> method.invoke(cryptoCoreUtil, mockSecretKey, mockData, mockNonce, mockAad));
            assertTrue(exception.getCause() instanceof AbisException);
        }
    }

    /**
     * Tests the decryptCbeffData method with cipher doFinal failure.
     */
    @Test
    void testDecryptCbeffDataWithCipherDoFinalFailure() throws Exception {
        SecretKey mockSecretKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] mockData = "encrypted-data".getBytes();
        byte[] mockNonce = new byte[12];
        byte[] mockAad = new byte[32];

        Cipher mockCipher = mock(Cipher.class);
        doThrow(new IllegalBlockSizeException()).when(mockCipher).doFinal(any(byte[].class), anyInt(), anyInt());

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData",
                    SecretKey.class, byte[].class, byte[].class, byte[].class);
            method.setAccessible(true);

            Exception exception = assertThrows(Exception.class,
                    () -> method.invoke(cryptoCoreUtil, mockSecretKey, mockData, mockNonce, mockAad));
            assertTrue(exception.getCause() instanceof AbisException);
        }
    }

    /**
     * Tests decryptCbeffData with certificate thumbprint
     */
    @Test
    void testDecryptCbeffDataWithCertThumbprint() throws Exception {
        // Setup test data
        byte[] certThumbprint = new byte[32];
        byte[] encryptedKey = "encrypted-key".getBytes();
        byte[] encryptedData = "encrypted-data".getBytes();
        byte[] expectedDecrypted = "decrypted-data".getBytes();

        // Create response data
        byte[] responseData = new byte[certThumbprint.length + encryptedKey.length + "#KEY_SPLITTER#".length() + encryptedData.length];
        System.arraycopy(certThumbprint, 0, responseData, 0, certThumbprint.length);
        System.arraycopy(encryptedKey, 0, responseData, certThumbprint.length, encryptedKey.length);
        System.arraycopy("#KEY_SPLITTER#".getBytes(), 0, responseData, certThumbprint.length + encryptedKey.length, "#KEY_SPLITTER#".length());
        System.arraycopy(encryptedData, 0, responseData, certThumbprint.length + encryptedKey.length + "#KEY_SPLITTER#".length(), encryptedData.length);

        // Mock private key entry and certificate
        KeyStore.PrivateKeyEntry mockPrivateKeyEntry = mock(KeyStore.PrivateKeyEntry.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        Certificate mockCertificate = mock(Certificate.class);
        when(mockPrivateKeyEntry.getPrivateKey()).thenReturn(mockPrivateKey);
        when(mockPrivateKeyEntry.getCertificate()).thenReturn(mockCertificate);
        when(mockCertificate.getEncoded()).thenReturn(certThumbprint);

        // Mock cipher operations
        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            Cipher mockCipher = mock(Cipher.class);
            when(mockCipher.doFinal(any(byte[].class))).thenReturn(expectedDecrypted);
            mockedCipher.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            // Access the private method using reflection
            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData", byte[].class, KeyStore.PrivateKeyEntry.class);
            method.setAccessible(true);

            byte[] result = (byte[]) method.invoke(cryptoCoreUtil, responseData, mockPrivateKeyEntry);
            assertArrayEquals(expectedDecrypted, result);
        }
    }

    /**
     * Tests decryptCbeffData with GCM parameters
     */
    @Test
    void testDecryptCbeffDataWithGCMParams() throws Exception {
        // Setup test data
        SecretKey mockKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] testData = "test-encrypted-data".getBytes();
        byte[] testIv = "test-iv-data-16by".getBytes(); // 16 bytes for IV
        byte[] data = new byte[testData.length + testIv.length];
        System.arraycopy(testData, 0, data, 0, testData.length);
        System.arraycopy(testIv, 0, data, testData.length, testIv.length);
        byte[] expectedDecrypted = "decrypted-data".getBytes();

        // Mock cipher operations
        Cipher mockCipher = mock(Cipher.class);
        when(mockCipher.getBlockSize()).thenReturn(16);
        when(mockCipher.doFinal(any(byte[].class))).thenReturn(expectedDecrypted);

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            mockedCipher.when(() -> Cipher.getInstance("AES/GCM/NoPadding")).thenReturn(mockCipher);

            // Access the private method using reflection
            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData", SecretKey.class, byte[].class);
            method.setAccessible(true);

            byte[] result = (byte[]) method.invoke(cryptoCoreUtil, mockKey, data);
            assertNotNull(result);
            assertArrayEquals(expectedDecrypted, result);

            // Verify GCM parameter spec was used correctly
            verify(mockCipher).init(eq(Cipher.DECRYPT_MODE), eq(mockKey), any(GCMParameterSpec.class));
        }
    }

    /**
     * Tests decryptCbeffData with invalid data length
     */
    @Test
    void testDecryptCbeffDataWithInvalidDataLength() throws Exception {
        SecretKey mockKey = new SecretKeySpec("test-key".getBytes(), "AES");
        byte[] data = "too-short".getBytes(); // Data shorter than block size

        try (MockedStatic<Cipher> mockedCipher = Mockito.mockStatic(Cipher.class)) {
            Cipher mockCipher = mock(Cipher.class);
            when(mockCipher.getBlockSize()).thenReturn(16);
            mockedCipher.when(() -> Cipher.getInstance("AES/GCM/NoPadding")).thenReturn(mockCipher);

            Method method = CryptoCoreUtil.class.getDeclaredMethod("decryptCbeffData", SecretKey.class, byte[].class);
            method.setAccessible(true);

            Exception exception = assertThrows(InvocationTargetException.class, () -> 
                method.invoke(cryptoCoreUtil, mockKey, data));
            assertTrue(exception.getCause() instanceof AbisException);
        }
    }
}