package org.biometric.provider;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockedStatic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InfoRequestTest {

    private InfoRequest infoRequest;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    // Set up the InfoRequest instance, mocks, and configure the temporary directory so the file-based methods fail.
    @BeforeEach
    public void setUp() throws Exception {
        infoRequest = new InfoRequest("8080");
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        Mockito.when(response.getWriter()).thenReturn(printWriter);
        System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
    }

    // Test method for MOSIPDINFO POST request verifying that a missing certificate file throws a NullPointerException.
    @Test
    public void testDoPostMissingCertificate() throws Exception {
        Mockito.when(request.getMethod()).thenReturn("MOSIPDINFO");
        assertThrows(NullPointerException.class, () -> infoRequest.doPost(request, response));
    }

    // Test method for OPTIONS request that verifies the content type is set via CORSManager.
    @Test
    public void testServiceOptions() throws Exception {
        try (MockedStatic<CORSManager> mockedCORS = Mockito.mockStatic(CORSManager.class)) {
            // Stub doOptions to set the content type on response
            mockedCORS.when(() -> CORSManager.doOptions(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
                HttpServletResponse resArg = invocation.getArgument(1);
                resArg.setContentType("application/json");
                return null;
            });
            Mockito.when(request.getMethod()).thenReturn("OPTIONS");
            infoRequest.service(request, response);
            Mockito.verify(response, Mockito.atLeastOnce()).setContentType(Mockito.anyString());
        }
    }

    // Test method for GET request that verifies the content type is set via CORSManager.
    @Test
    public void testServiceGET() throws Exception {
        try (MockedStatic<CORSManager> mockedCORS = Mockito.mockStatic(CORSManager.class)) {
            // Stub doOptions to set the content type on response
            mockedCORS.when(() -> CORSManager.doOptions(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
                HttpServletResponse resArg = invocation.getArgument(1);
                resArg.setContentType("application/json");
                return null;
            });
            Mockito.when(request.getMethod()).thenReturn("GET");
            infoRequest.service(request, response);
            Mockito.verify(response, Mockito.atLeastOnce()).setContentType(Mockito.anyString());
        }
    }

    // Test method to verify that an invalid digital finger ID file returns a null value.
    @Test
    public void testGetDigitalFingerIdForInvalidFile() {
        String result = infoRequest.getDigitalFingerId("FIR");
        assertNull(result);
    }
}