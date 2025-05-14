package io.mosip.proxy.abis.utility;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test class for Helpers utility class.
 * This class tests the private constructor and utility methods for reading files and streams from resources.
 */
@ExtendWith(MockitoExtension.class)
class HelpersTest {

    private static final String TEST_FILE = "test.txt"; // Name of the test file
    private static final String TEST_CONTENT = "Hello World!"; // Content of the test file

    /**
     * Tests the private constructor of the Helpers class.
     * Verifies that it throws an IllegalStateException when invoked.
     */
    @Test
    void testPrivateConstructor() throws Exception {
        // Access the private constructor of Helpers
        Constructor<Helpers> constructor = Helpers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Verify that invoking the constructor throws an IllegalStateException
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Helpers class", exception.getCause().getMessage());
    }

    /**
     * Tests the readFileFromResources method for a valid file.
     * Verifies that the file content is read correctly.
     */
    @Test
    void testReadFileFromResources_Success() throws IOException {
        // Create a test file in the resources directory
        createTestFile();

        // Read the file content
        String content = Helpers.readFileFromResources(TEST_FILE);

        // Verify the content matches the expected value
        assertNotNull(content);
        assertEquals(TEST_CONTENT, content.trim());
    }

    /**
     * Tests the readFileFromResources method for a non-existent file.
     * Verifies that it throws a NullPointerException.
     */
    @Test
    void testReadFileFromResources_FileNotFound() {
        assertThrows(NullPointerException.class,
                () -> Helpers.readFileFromResources("nonexistent.txt"));
    }

    /**
     * Tests the readStreamFromResources method for a valid file.
     * Verifies that the input stream content is read correctly.
     */
    @Test
    void testReadStreamFromResources_Success() throws IOException {
        // Create a test file in the resources directory
        createTestFile();

        // Get the input stream for the file
        InputStream stream = Helpers.readStreamFromResources(TEST_FILE);

        // Verify the stream content matches the expected value
        assertNotNull(stream);
        String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
        assertEquals(TEST_CONTENT, content.trim());
    }

    /**
     * Tests the readStreamFromResources method for a non-existent file.
     * Verifies that it returns null.
     */
    @Test
    void testReadStreamFromResources_FileNotFound() {
        InputStream stream = Helpers.readStreamFromResources("nonexistent.txt");
        assertNull(stream);
    }

    /**
     * Helper method to create a test file in the resources directory.
     * Writes the test content to the specified file.
     */
    private void createTestFile() throws IOException {
        // Create a test file in the test resources directory
        try (InputStream is = new java.io.ByteArrayInputStream(TEST_CONTENT.getBytes())) {
            // Ensure the resources directory exists
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/test/resources"));
            // Write the test content to the file
            java.nio.file.Files.copy(is,
                    java.nio.file.Paths.get("src/test/resources/" + TEST_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}