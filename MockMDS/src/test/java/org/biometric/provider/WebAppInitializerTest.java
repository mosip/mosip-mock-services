package org.biometric.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;

import static org.junit.jupiter.api.Assertions.assertThrows;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.DispatcherServlet;

class WebAppInitializerTest {

    private WebAppInitializer webAppInitializer;
    private ServletContext mockServletContext;
    private ServletRegistration.Dynamic mockServletRegistration;

    /**
     * Sets up test environment before each test.
     * Creates mock objects and initializes the WebAppInitializer instance.
     */
    @BeforeEach
    void setUp() {
        webAppInitializer = new WebAppInitializer();
        mockServletContext = mock(ServletContext.class);
        mockServletRegistration = mock(ServletRegistration.Dynamic.class);
        when(mockServletContext.addServlet(anyString(), any(DispatcherServlet.class)))
                .thenReturn(mockServletRegistration);
    }

    /**
     * Tests the onStartup method to verify:
     * - Servlet registration with correct name
     * - Load on startup is set to 1
     * - URL mapping is set to "/"
     */
    @Test
    void onStartup_success() throws ServletException {
        webAppInitializer.onStartup(mockServletContext);
        verify(mockServletContext).addServlet(eq("dispatcherExample"), any(DispatcherServlet.class));
        verify(mockServletRegistration).setLoadOnStartup(1);
        verify(mockServletRegistration).addMapping("/");
    }

    /**
     * Tests error handling when servlet registration fails.
     * Verifies that RuntimeException is properly handled.
     */
    @Test
    void onStartupWithError_failure() {
        when(mockServletContext.addServlet(anyString(), any(DispatcherServlet.class)))
                .thenThrow(new RuntimeException("Servlet registration failed"));

        assertThrows(RuntimeException.class, () -> {
            webAppInitializer.onStartup(mockServletContext);
        });
        verify(mockServletContext).addServlet(anyString(), any(DispatcherServlet.class));
    }
}