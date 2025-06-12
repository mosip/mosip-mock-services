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

class InfoRequestTest {

    private InfoRequest infoRequest;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    /**
     * Sets up the InfoRequest instance, mocks, and configures the temporary directory.
     */
    @BeforeEach
    void setUp() throws Exception {
        infoRequest = new InfoRequest("8080");
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        Mockito.when(response.getWriter()).thenReturn(printWriter);
        System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
    }

    /**
     * Tests doPost method for MOSIPDINFO POST request.
     * Verifies that a missing certificate file throws a NullPointerException.
     */
    @Test
    void doPost_missingCertificate_throwsNullPointerException() throws Exception {
        Mockito.when(request.getMethod()).thenReturn("MOSIPDINFO");
        assertThrows(NullPointerException.class, () -> infoRequest.doPost(request, response));
    }

    /**
     * Tests service method for OPTIONS request.
     * Verifies that the content type is set via CORSManager.
     */
    @Test
    void service_optionsRequest_success() throws Exception {
        try (MockedStatic<CORSManager> mockedCORS = Mockito.mockStatic(CORSManager.class)) {
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

    /**
     * Tests service method for GET request.
     * Verifies that the content type is set via CORSManager.
     */
    @Test
    void service_getRequest_success() throws Exception {
        try (MockedStatic<CORSManager> mockedCORS = Mockito.mockStatic(CORSManager.class)) {
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

    /**
     * Tests getDigitalFingerId method for an invalid digital finger ID file.
     * Verifies that it returns a null value.
     */
    @Test
    void getDigitalFingerId_invalidFile_returnsNull() {
        String result = infoRequest.getDigitalFingerId("FIR");
        assertNull(result);
    }
}
