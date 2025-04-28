package io.mosip.proxy.abis.utility;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpersTest {


    private static final String TEST_FILE = "test.txt";
    private static final String TEST_CONTENT = "Hello World!";

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<Helpers> constructor = Helpers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Helpers class", exception.getCause().getMessage());
    }

    @Test
    void testReadFileFromResources_Success() throws IOException {
        // Create test file in resources
        createTestFile();

        // Read file content
        String content = Helpers.readFileFromResources(TEST_FILE);

        // Verify content
        assertNotNull(content);
        assertEquals(TEST_CONTENT, content.trim());
    }

    @Test
    void testReadFileFromResources_FileNotFound() {
        assertThrows(NullPointerException.class,
                () -> Helpers.readFileFromResources("nonexistent.txt"));
    }

    @Test
    void testReadStreamFromResources_Success() throws IOException {
        // Create test file in resources
        createTestFile();

        // Get input stream
        InputStream stream = Helpers.readStreamFromResources(TEST_FILE);

        // Verify stream content
        assertNotNull(stream);
        String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
        assertEquals(TEST_CONTENT, content.trim());
    }

    @Test
    void testReadStreamFromResources_FileNotFound() {
        InputStream stream = Helpers.readStreamFromResources("nonexistent.txt");
        assertNull(stream);
    }

    private void createTestFile() throws IOException {
        // Create a test file in the test resources directory
        try (InputStream is = new java.io.ByteArrayInputStream(TEST_CONTENT.getBytes())) {
            // Copy test content to test resources
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/test/resources"));
            java.nio.file.Files.copy(is,
                    java.nio.file.Paths.get("src/test/resources/" + TEST_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}