package io.mosip.mock.sbi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileHelperTest {

    private Path tempDir;
    private File testFile;
    private static final String TEST_CONTENT = "test content";

    /**
     * Sets up test environment by creating a temporary directory and file
     * with test content before each test.
     */
    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("filehelper_test");
        testFile = new File(tempDir.toFile(), "test.txt");
        Files.write(testFile.toPath(), TEST_CONTENT.getBytes());
    }

    /**
     * Cleans up test environment by deleting temporary files and directory
     * after each test.
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile.toPath());
        Files.deleteIfExists(tempDir);
    }

    /**
     * Tests file existence check functionality.
     * Verifies both existing and non-existing file cases.
     */
    @Test
    void exists_existingFile_returnsTrue() {
        assertTrue(FileHelper.exists(testFile.getAbsolutePath()));
        assertFalse(FileHelper.exists(tempDir + "/nonexistent.txt"));
    }

    /**
     * Tests directory existence check functionality.
     * Verifies both existing and non-existing directory cases.
     */
    @Test
    void directoryExists_existingDirectory_returnsTrue() {
        assertTrue(FileHelper.directoryExists(tempDir.toString()));
        assertFalse(FileHelper.directoryExists(tempDir + "/nonexistent"));
    }

    /**
     * Tests reading file contents as byte array using file path.
     * Verifies correct content reading from file.
     */
    @Test
    void readAllBytes_validFilePath_returnsContent() throws IOException {
        byte[] content = FileHelper.readAllBytes(testFile.getAbsolutePath());
        assertArrayEquals(TEST_CONTENT.getBytes(), content);
    }

    /**
     * Tests reading file contents as byte array using File object.
     * Verifies correct content reading from file.
     */
    @Test
    void loadFile_validFile_returnsContent() throws IOException {
        byte[] content = FileHelper.loadFile(testFile);
        assertArrayEquals(TEST_CONTENT.getBytes(), content);
    }

    /**
     * Tests retrieving canonical path of current directory.
     * Verifies path existence and validity.
     */
    @Test
    void getCanonicalPath_success() throws IOException {
        String path = FileHelper.getCanonicalPath();
        assertNotNull(path);
        assertTrue(new File(path).exists());
    }

    /**
     * Tests retrieving system temporary directory path.
     * Verifies path existence and validity.
     */
    @Test
    void TempDirectory_success() {
        String tempDir = FileHelper.getUserTempDirectory();
        assertNotNull(tempDir);
        assertTrue(new File(tempDir).exists());
    }

    /**
     * Tests retrieving operating system architecture.
     * Verifies non-null and non-empty response.
     */
    @Test
    void getOS_success() {
        String os = FileHelper.getOS();
        assertNotNull(os);
        assertFalse(os.isEmpty());
    }
}
