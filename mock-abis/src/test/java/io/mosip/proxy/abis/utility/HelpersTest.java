package io.mosip.proxy.abis.utility;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    void testHelpers_PrivateConstructor_ThrowsIllegalStateException() throws Exception {
        Constructor<Helpers> constructor = Helpers.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Helpers class", exception.getCause().getMessage());
    }

    /**
     * Tests the readFileFromResources method for a non-existent file.
     * Verifies that it throws a NullPointerException.
     */
    @Test
    void testReadFileFromResources_FileNotFound_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> Helpers.readFileFromResources("nonexistent.txt"));
    }

    /**
     * Tests the readStreamFromResources method for a non-existent file.
     * Verifies that it returns null.
     */
    @Test
    void testReadStreamFromResources_FileNotFound_ReturnsNull() {
        InputStream stream = Helpers.readStreamFromResources("nonexistent.txt");
        assertNull(stream);
    }

    /**
     * Helper method to create a test file in the resources directory.
     * Writes the test content to the specified file.
     */
    private void createTestFile() throws IOException {
        try (InputStream is = new java.io.ByteArrayInputStream(TEST_CONTENT.getBytes())) {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/test/resources"));
            java.nio.file.Files.copy(is,
                    java.nio.file.Paths.get("src/test/resources/" + TEST_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}