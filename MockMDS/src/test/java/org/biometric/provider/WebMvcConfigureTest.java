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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebMvcConfigureTest {

    private WebMvcConfigure webMvcConfigure;
    private MockMvc mockMvc;
    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        when(environment.getProperty("server.port")).thenReturn("8080");
        webMvcConfigure = new WebMvcConfigure(environment);
        mockMvc = MockMvcBuilders.standaloneSetup(webMvcConfigure).build();
    }

    /**
     * Tests that default servlet handling is properly configured by enabling the configurer.
     */
    @Test
    void configureDefaultServletHandling_success() {
        DefaultServletHandlerConfigurer configurer = mock(DefaultServletHandlerConfigurer.class);
        webMvcConfigure.configureDefaultServletHandling(configurer);
        verify(configurer).enable();
    }

    /**
     * Tests that the infoBean method returns a valid servlet registration with correct URL mapping and type.
     */
    @Test
    void infoBean_success() {
        ServletRegistrationBean<?> bean = webMvcConfigure.infoBean();
        assertNotNull(bean, "Info servlet bean should not be null");
        assertEquals("/info", bean.getUrlMappings().iterator().next(), "URL mapping should be /info");
        assertTrue(bean.getServlet() instanceof InfoRequest, "Servlet should be instance of InfoRequest");
    }

    /**
     * Tests that the discoverBean method returns a valid servlet registration with correct URL mapping and type.
     */
    @Test
    void discoverBean_success() {
        ServletRegistrationBean<?> bean = webMvcConfigure.discoverBean();
        assertNotNull(bean, "Discover servlet bean should not be null");
        assertEquals("/device", bean.getUrlMappings().iterator().next(), "URL mapping should be /device");
        assertTrue(bean.getServlet() instanceof DiscoverRequest, "Servlet should be instance of DiscoverRequest");
    }

    /**
     * Tests that the captureBean method returns a valid servlet registration with correct URL mapping and type.
     */
    @Test
    void captureBean_success() {
        ServletRegistrationBean<?> bean = webMvcConfigure.captureBean();
        assertNotNull(bean, "Capture servlet bean should not be null");
        assertEquals("/capture", bean.getUrlMappings().iterator().next(), "URL mapping should be /capture");
        assertTrue(bean.getServlet() instanceof CaptureRequest, "Servlet should be instance of CaptureRequest");
    }

    /**
     * Tests that the streamBean method returns a valid servlet registration with correct URL mapping and type.
     */
    @Test
    void streamBean_success() {
        ServletRegistrationBean<?> bean = webMvcConfigure.streamBean();
        assertNotNull(bean, "Stream servlet bean should not be null");
        assertEquals("/stream", bean.getUrlMappings().iterator().next(), "URL mapping should be /stream");
        assertTrue(bean.getServlet() instanceof StreamRequest, "Servlet should be instance of StreamRequest");
    }

    /**
     * Tests that the properties() method returns a non-null PropertySourcesPlaceholderConfigurer.
     */
    @Test
    void propertiesBean_success() {
        PropertySourcesPlaceholderConfigurer configurer = WebMvcConfigure.properties();
        assertNotNull(configurer, "Properties configurer should not be null");
    }

    /**
     * Tests that the WebMvcConfigure instance has been initialized.
     */
    @Test
    void webMvcConfigureInitialization_success() {
        assertNotNull(webMvcConfigure, "WebMvcConfigure instance should not be null");
    }

    /**
     * Tests that the MockMvc instance is configured properly.
     */
    @Test
    void mockMvcConfiguration_success() {
        assertNotNull(mockMvc, "MockMvc should be properly configured");
    }

    /**
     * Tests that environment properties can be accessed using the Environment mock.
     */
    @Test
    void environmentPropertyAccess_success() {
        when(environment.getProperty("custom.property")).thenReturn("test-value");
        assertEquals("test-value", environment.getProperty("custom.property"));
        verify(environment, times(1)).getProperty("custom.property");
    }
}
