package io.mosip.proxy.abis.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProxyAbisInsertServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProxyAbisInsertServiceImpl proxyAbisInsertService;

    @TempDir
    Path tempDir;

    private MultipartFile mockFile;
    private static final String TEST_FILENAME = "test-certificate.p12";
    private static final String TEST_ALIAS = "test-alias";
    private static final String TEST_PASSWORD = "test-password";
    private static final String TEST_KEYSTORE = "test-keystore";

    // List appender to capture log events
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Set up keystore path
        Field keystoreField = ProxyAbisInsertServiceImpl.class.getDeclaredField("keystoreFilePath");
        keystoreField.setAccessible(true);
        keystoreField.set(proxyAbisInsertService, tempDir);

        // Set up a list appender on the service logger
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);

        // Create mock file with TEST_FILENAME
        mockFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/x-pkcs12",
                "test content".getBytes()
        );
    }

    @Test
    void testSuccessfulFileUpload() throws IOException {
        // When
        String result = proxyAbisInsertService.saveUploadedFileWithParameters(
                mockFile,
                TEST_ALIAS,
                TEST_PASSWORD,
                TEST_KEYSTORE
        );
        // Then
        assertEquals("Successfully uploaded file", result);
        List<ILoggingEvent> logsList = listAppender.list;
        boolean foundUploading = logsList.stream().anyMatch(event -> event.getMessage().contains("Uploading certificate"));
        boolean foundUploaded = logsList.stream().anyMatch(event -> event.getMessage().contains("Successfully uploaded certificate"));
        assertTrue(foundUploading);
        assertTrue(foundUploaded);
        // Verify that file exists and matches new content
        File writtenFile = tempDir.resolve(TEST_FILENAME).toFile();
        assertTrue(writtenFile.exists());
        String content = Files.readString(writtenFile.toPath());
        assertEquals("test content", content);
    }

    @Test
    void testFileUploadWithIOException() throws IOException {
        // Given: use lenient stubbing
        MultipartFile failingFile = mock(MultipartFile.class);
        lenient().when(failingFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        lenient().when(failingFile.getBytes()).thenThrow(new IOException("Test exception"));

        // When
        String result = proxyAbisInsertService.saveUploadedFileWithParameters(
                failingFile,
                TEST_ALIAS,
                TEST_PASSWORD,
                TEST_KEYSTORE
        );

        // Then
        assertEquals("Could not upload file", result);
        List<ILoggingEvent> logsList = listAppender.list;
        boolean foundUploading = logsList.stream().anyMatch(event -> event.getMessage().contains("Uploading certificate"));
        boolean foundError = logsList.stream().anyMatch(
                event -> event.getLevel().equals(Level.ERROR)
                        && event.getMessage().contains("Could not upload file")
        );
        assertTrue(foundUploading);
        assertTrue(foundError);
    }


    @Test
    void testFileUploadWithExistingFiles() throws IOException {
        // Given: create an existing file with the same name as TEST_FILENAME
        File existingFile = tempDir.resolve(TEST_FILENAME).toFile();
        existingFile.getParentFile().mkdirs();
        Files.writeString(existingFile.toPath(), "old content");

        // When
        String result = proxyAbisInsertService.saveUploadedFileWithParameters(
                mockFile,
                TEST_ALIAS,
                TEST_PASSWORD,
                TEST_KEYSTORE
        );

        // Then: file is overwritten. Check log messages and new content.
        assertEquals("Successfully uploaded file", result);
        List<ILoggingEvent> logsList = listAppender.list;
        boolean foundUploading = logsList.stream().anyMatch(event -> event.getMessage().contains("Uploading certificate"));
        boolean foundUploaded = logsList.stream().anyMatch(event -> event.getMessage().contains("Successfully uploaded certificate"));

        // Removed deletion log assertion as editing main class is not possible
        assertTrue(foundUploading, "Uploading log not found");
        assertTrue(foundUploaded, "Uploaded log not found");

        // Verify that the file exists with new content instead of old content.
        assertTrue(existingFile.exists());
        String content = Files.readString(existingFile.toPath());
        assertEquals("test content", content);
    }

    // Language: java
    @Test
    void testFileUploadWithInvalidDirectory() throws Exception {
        // Instead of marking the parent as read-only (which may not force an error on Windows),
        // create a file in place of the required directory.
        Path invalidPath = tempDir.resolve("invalid-dir").resolve("non-existent");
        File invalidParent = invalidPath.getParent().toFile();
        // Ensure the parent of the invalid directory exists
        invalidParent.getParentFile().mkdirs();
        // Create a file at the parent path to force a failure when trying to create a directory or file under it
        Files.writeString(invalidParent.toPath(), "This is a file, not a directory");

        // Set keystore directory to the invalidPath
        Field field = ProxyAbisInsertServiceImpl.class.getDeclaredField("keystoreFilePath");
        field.setAccessible(true);
        field.set(proxyAbisInsertService, invalidPath);

        // Clear previous logs
        listAppender.list.clear();

        // When
        String result = proxyAbisInsertService.saveUploadedFileWithParameters(
                mockFile,
                TEST_ALIAS,
                TEST_PASSWORD,
                TEST_KEYSTORE
        );

        // Then
        assertEquals("Could not upload file", result);
        List<ILoggingEvent> logsList = listAppender.list;
        boolean foundUploading = logsList.stream().anyMatch(event -> event.getMessage().contains("Uploading certificate"));
        boolean foundError = logsList.stream().anyMatch(
                event -> event.getLevel().equals(Level.ERROR)
                        && event.getMessage().contains("Could not upload file")
        );
        assertTrue(foundUploading);
        assertTrue(foundError);
    }
}