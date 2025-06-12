package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

/**
 * Test class for SBIWorker
 * Verifies the handling of HTTP requests, response generation,
 * and error scenarios in the SBI worker implementation
 */
class SBIWorkerTest {

    @Mock
    private SBIMockService mockService;

    @Mock
    private Socket clientSocket;

    private SBIWorker sbiWorker;
    private AutoCloseable mockitoCloseable;
    private ByteArrayOutputStream outputStream;
    private BufferedOutputStream bufferedOutputStream;

    private final int TEST_PORT = 8080;

    /**
     * Sets up test environment before each test
     * Initializes mocks, output streams, and worker instance
     */
    @BeforeEach
    void setUp() throws IOException {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        bufferedOutputStream = new BufferedOutputStream(outputStream);

        when(clientSocket.getOutputStream()).thenReturn(bufferedOutputStream);
        when(clientSocket.getInputStream()).thenReturn(
                new BufferedInputStream(new ByteArrayInputStream(new byte[0]))
        );
        sbiWorker = new SBIWorker(mockService, clientSocket, TEST_PORT);
    }

    /**
     * Cleans up resources after each test
     * Closes streams and Mockito resources
     */
    @AfterEach
    void tearDown() throws Exception {
        if (bufferedOutputStream != null) {
            bufferedOutputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    /**
     * Tests constructor initialization
     * Verifies that all dependencies are properly set
     */
    @Test
    void constructor_initialization_success() {
        assertNotNull(sbiWorker);
        assertEquals(mockService, sbiWorker.getMockService());
        assertEquals(clientSocket, sbiWorker.getClientSocket());
        assertEquals(TEST_PORT, sbiWorker.getServerPort());
    }

    /**
     * Tests getter and setter methods
     * Verifies proper setting and retrieval of worker properties
     */
    @Test
    void getterSetter_methods_success() {
        SBIMockService newMockService = mock(SBIMockService.class);
        Socket newSocket = mock(Socket.class);
        int newPort = 9090;

        sbiWorker.setMockService(newMockService);
        sbiWorker.setClientSocket(newSocket);
        sbiWorker.setServerPort(newPort);

        assertEquals(newMockService, sbiWorker.getMockService());
        assertEquals(newSocket, sbiWorker.getClientSocket());
        assertEquals(newPort, sbiWorker.getServerPort());
    }

    /**
     * Tests handling of valid OPTIONS request
     * Verifies correct response generation for OPTIONS method
     */
    @Test
    void run_withValidOptionsRequest_success() throws IOException {
        String request = "OPTIONS /info HTTP/1.1\r\nContent-Length: 0\r\n\r\n";
        setupInputStreamMock(request);

        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS))
                    .thenReturn("OPTIONS,CAPTURE,INFO");

            sbiWorker.run();

            String response = outputStream.toString(StandardCharsets.UTF_8);
            assertTrue(response.contains("HTTP/1.1 200 OK"));
        }
    }

    /**
     * Tests handling of invalid HTTP method
     * Verifies proper error response for unsupported methods
     */
    @Test
    void run_withInvalidMethod_errorResponse() throws IOException {
        String request = "INVALID /info HTTP/1.1\r\nContent-Length: 0\r\n\r\n";
        setupInputStreamMock(request);

        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS))
                    .thenReturn("OPTIONS,CAPTURE,INFO");

            sbiWorker.run();

            String response = outputStream.toString(StandardCharsets.UTF_8);
            assertTrue(response.contains("HTTP/1.1 405"));
        }
    }

    /**
     * Tests handling of IO exceptions
     * Verifies proper error handling for socket operations
     */
    @Test
    void run_withIOException_handlingSuccess() throws IOException {
        SBIWorker testWorker = new SBIWorker(mockService, clientSocket, TEST_PORT);
        String testRequest = "GET / HTTP/1.1\r\n\r\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(testRequest.getBytes(StandardCharsets.UTF_8));
        when(clientSocket.getInputStream()).thenReturn(new BufferedInputStream(bis));

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        BufferedOutputStream mockBos = new BufferedOutputStream(mockOutput) {
            @Override
            public void write(byte[] b) throws IOException {
                throw new IOException("Test write exception");
            }

            @Override
            public void close() throws IOException {
            }
        };
        when(clientSocket.getOutputStream()).thenReturn(mockBos);
        doNothing().when(clientSocket).close();
        assertDoesNotThrow(() -> {
            testWorker.run();
        });

        verify(clientSocket, times(1)).close();
    }

    /**
     * Tests handling of valid CAPTURE request
     * Verifies correct processing of capture requests
     */
    @Test
    void run_withValidCaptureRequest_success() throws IOException {
        String request = "CAPTURE /capture HTTP/1.1\r\nContent-Length: 2\r\n\r\n{}";
        setupInputStreamMock(request);

        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS))
                    .thenReturn("OPTIONS,CAPTURE,INFO");

            sbiWorker.run();

            String response = outputStream.toString(StandardCharsets.UTF_8);
            assertTrue(response.contains("HTTP/1.1"));
        }
    }

    /**
     * Tests handling of empty requests
     * Verifies proper error response for empty request content
     */
    @Test
    void run_withEmptyRequest_errorResponse() throws IOException {
        String request = "\r\n";
        setupInputStreamMock(request);

        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS))
                    .thenReturn("OPTIONS,CAPTURE,INFO");

            sbiWorker.run();

            String response = outputStream.toString(StandardCharsets.UTF_8);
            assertTrue(response.contains("HTTP/1.1 405"));
        }
    }

    /**
     * Helper method to set up input stream mock
     * Creates a BufferedInputStream with the given request string
     */
    private void setupInputStreamMock(String request) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                request.getBytes(StandardCharsets.UTF_8)
        );
        when(clientSocket.getInputStream()).thenReturn(new BufferedInputStream(inputStream));
    }
}
