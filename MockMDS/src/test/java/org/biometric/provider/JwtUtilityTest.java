package org.biometric.provider;

import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

class JwtUtilityTest {

    private JwtUtility jwtUtility;
    private X509Certificate mockCertificate;
    private PrivateKey privateKey;
    private String originalUserDir;

    @TempDir
    Path tempDir;

    /**
     * Sets up the test environment by initializing JwtUtility, generating a key pair,
     * and saving the original user directory system property.
     */
    @BeforeEach
    void setUp() throws Exception {
        jwtUtility = new JwtUtility();
        mockCertificate = mock(X509Certificate.class);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        originalUserDir = System.getProperty("user.dir");
    }

    /**
     * Tests the getJwt method via reflection and asserts that the returned JWT token is not null
     * and contains the expected dot separators.
     */
    @Test
    void testGetJwt() throws Exception {
        Method getJwtMethod = JwtUtility.class.getDeclaredMethod("getJwt",
                byte[].class, PrivateKey.class, X509Certificate.class);
        getJwtMethod.setAccessible(true);

        byte[] testData = "test data".getBytes();
        String jwt = (String) getJwtMethod.invoke(jwtUtility, testData, privateKey, mockCertificate);
        assertNotNull(jwt, "JWT token should not be null");
        assertTrue(jwt.contains("."), "JWT token should contain dots as separators");
    }

    /**
     * Tests the getCertificate method by setting an invalid PEM file in the temporary directory
     * and asserting that the returned certificate is null.
     */
    @Test
    void testGetCertificate() throws Exception {
        Method getCertificateMethod = JwtUtility.class.getDeclaredMethod("getCertificate");
        getCertificateMethod.setAccessible(true);

        try {
            System.setProperty("user.dir", tempDir.toString());
            createTestPemFile("MosipTestCert.pem", "CERTIFICATE");

            X509Certificate cert = (X509Certificate) getCertificateMethod.invoke(jwtUtility);
            assertNull(cert, "Certificate should be null for invalid test data");
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    /**
     * Tests the getPrivateKey method by setting an invalid PEM file in the temporary directory
     * and asserting that the returned private key is null.
     */
    @Test
    void testGetPrivateKey() throws Exception {
        Method getPrivateKeyMethod = JwtUtility.class.getDeclaredMethod("getPrivateKey");
        getPrivateKeyMethod.setAccessible(true);

        try {
            System.setProperty("user.dir", tempDir.toString());
            createTestPemFile("PrivateKey.pem", "PRIVATE KEY");

            PrivateKey key = (PrivateKey) getPrivateKeyMethod.invoke(jwtUtility);
            assertNull(key, "Private key should be null for invalid test data");
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    /**
     * Tests the getFileContent method by creating a temporary file with known content
     * and asserting that the method returns the expected content.
     */
    @Test
    void testGetFileContent() throws Exception {
        Method getFileContentMethod = JwtUtility.class.getDeclaredMethod("getFileContent",
                FileInputStream.class, Charset.class);
        getFileContentMethod.setAccessible(true);

        String testContent = "Test Content";
        File testFile = tempDir.resolve("test.txt").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(testContent);
        }

        String content = (String) getFileContentMethod.invoke(jwtUtility,
                new FileInputStream(testFile), Charset.defaultCharset());
        assertEquals(testContent + "\n", content, "File content should match");
    }

    /**
     * Tests the getPropertyValue method by mocking ApplicationPropertyHelper and asserting that
     * the returned property value matches the expected value.
     */
    @Test
    void testGetPropertyValue() throws Exception {
        Method getPropertyValueMethod = JwtUtility.class.getDeclaredMethod("getPropertyValue", String.class);
        getPropertyValueMethod.setAccessible(true);

        String testKey = "test.key";
        String expectedValue = "test.value";

        try (MockedStatic<ApplicationPropertyHelper> mockedHelper =
                     Mockito.mockStatic(ApplicationPropertyHelper.class)) {
            mockedHelper.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(anyString()))
                    .thenReturn(expectedValue);

            String actualValue = (String) getPropertyValueMethod.invoke(jwtUtility, testKey);
            assertEquals(expectedValue, actualValue, "Property value should match");
        }
    }

    /**
     * Creates a test PEM file of the specified type in a temporary directory.
     * This helper method is used by the certificate and private key tests.
     *
     * @param fileName the name of the PEM file to create.
     * @param type     the type of the PEM content (e.g., CERTIFICATE, PRIVATE KEY).
     */
    private void createTestPemFile(String fileName, String type) {
        try {
            File keysDir = new File(tempDir.toString(), "files/keys");
            keysDir.mkdirs();
            File pemFile = new File(keysDir, fileName);
            try (FileWriter writer = new FileWriter(pemFile)) {
                writer.write("-----BEGIN " + type + "-----\n");
                writer.write(Base64.getEncoder().encodeToString("test".getBytes()));
                writer.write("\n-----END " + type + "-----");
            }
        } catch (Exception e) {
            fail("Failed to create test PEM file: " + e.getMessage());
        }
    }
}