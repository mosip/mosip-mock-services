package org.biometric.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WebMvcConfigureTest {

    private WebMvcConfigure webMvcConfigure;
    private MockMvc mockMvc;
    private Environment environment;

    // Initializes mocks and sets up the WebMvcConfigure instance and MockMvc before each test.
    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        when(environment.getProperty("server.port")).thenReturn("8080");
        webMvcConfigure = new WebMvcConfigure(environment);
        mockMvc = MockMvcBuilders.standaloneSetup(webMvcConfigure).build();
    }

    // Tests that default servlet handling is properly configured by enabling the configurer.
    @Test
    void testConfigureDefaultServletHandling() {
        DefaultServletHandlerConfigurer configurer = mock(DefaultServletHandlerConfigurer.class);
        webMvcConfigure.configureDefaultServletHandling(configurer);
        verify(configurer).enable();
    }

    // Tests that the infoBean method returns a valid servlet registration with correct URL mapping and type.
    @Test
    void testInfoBean() {
        ServletRegistrationBean<?> bean = webMvcConfigure.infoBean();
        assertNotNull(bean, "Info servlet bean should not be null");
        assertEquals("/info", bean.getUrlMappings().iterator().next(), "URL mapping should be /info");
        assertTrue(bean.getServlet() instanceof InfoRequest, "Servlet should be instance of InfoRequest");
    }

    // Tests that the discoverBean method returns a valid servlet registration with correct URL mapping and type.
    @Test
    void testDiscoverBean() {
        ServletRegistrationBean<?> bean = webMvcConfigure.discoverBean();
        assertNotNull(bean, "Discover servlet bean should not be null");
        assertEquals("/device", bean.getUrlMappings().iterator().next(), "URL mapping should be /device");
        assertTrue(bean.getServlet() instanceof DiscoverRequest, "Servlet should be instance of DiscoverRequest");
    }

    // Tests that the captureBean method returns a valid servlet registration with correct URL mapping and type.
    @Test
    void testCaptureBean() {
        ServletRegistrationBean<?> bean = webMvcConfigure.captureBean();
        assertNotNull(bean, "Capture servlet bean should not be null");
        assertEquals("/capture", bean.getUrlMappings().iterator().next(), "URL mapping should be /capture");
        assertTrue(bean.getServlet() instanceof CaptureRequest, "Servlet should be instance of CaptureRequest");
    }

    // Tests that the streamBean method returns a valid servlet registration with correct URL mapping and type.
    @Test
    void testStreamBean() {
        ServletRegistrationBean<?> bean = webMvcConfigure.streamBean();
        assertNotNull(bean, "Stream servlet bean should not be null");
        assertEquals("/stream", bean.getUrlMappings().iterator().next(), "URL mapping should be /stream");
        assertTrue(bean.getServlet() instanceof StreamRequest, "Servlet should be instance of StreamRequest");
    }

    // Tests that the properties() method returns a non-null PropertySourcesPlaceholderConfigurer.
    @Test
    void testPropertiesBean() {
        PropertySourcesPlaceholderConfigurer configurer = WebMvcConfigure.properties();
        assertNotNull(configurer, "Properties configurer should not be null");
    }

    // Tests that the WebMvcConfigure instance has been initialized.
    @Test
    void testWebMvcConfigureInitialization() {
        assertNotNull(webMvcConfigure, "WebMvcConfigure instance should not be null");
    }

    // Tests that the MockMvc instance is configured properly.
    @Test
    void testMockMvcConfiguration() {
        assertNotNull(mockMvc, "MockMvc should be properly configured");
    }

    // Tests that environment properties can be accessed using the Environment mock.
    @Test
    void testEnvironmentPropertyAccess() {
        when(environment.getProperty("custom.property")).thenReturn("test-value");
        assertEquals("test-value", environment.getProperty("custom.property"));
        verify(environment, times(1)).getProperty("custom.property");
    }
}