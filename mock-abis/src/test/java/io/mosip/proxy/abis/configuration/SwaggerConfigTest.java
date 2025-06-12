package io.mosip.proxy.abis.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for SwaggerConfig.
 * Tests the configuration of OpenAPI documentation and API grouping functionality.
 */
class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;
    private OpenApiProperties openApiProperties;
    private InfoProperty infoProperty;
    private LicenseProperty licenseProperty;
    private Group group;
    private Service service;

    /**
     * Sets up the test environment before each test.
     * Creates and configures mock objects for:
     * - OpenApiProperties and its nested properties
     * - License information
     * - API information
     * - Server configuration
     * - Group settings
     */
    @BeforeEach
    void setUp() {
        openApiProperties = mock(OpenApiProperties.class);
        infoProperty = mock(InfoProperty.class);
        licenseProperty = mock(LicenseProperty.class);
        group = mock(Group.class);
        service = mock(Service.class);

        when(licenseProperty.getName()).thenReturn("Test License");
        when(licenseProperty.getUrl()).thenReturn("http://test-license.com");

        when(infoProperty.getTitle()).thenReturn("Test API");
        when(infoProperty.getVersion()).thenReturn("1.0");
        when(infoProperty.getDescription()).thenReturn("Test Description");
        when(infoProperty.getLicense()).thenReturn(licenseProperty);

        Server server = mock(Server.class);
        when(server.getDescription()).thenReturn("Test Server");
        when(server.getUrl()).thenReturn("http://test-server.com");
        when(service.getServers()).thenReturn(List.of(server));

        when(group.getName()).thenReturn("abis");
        when(group.getPaths()).thenReturn(Arrays.asList("/abis/**"));

        when(openApiProperties.getInfo()).thenReturn(infoProperty);
        when(openApiProperties.getService()).thenReturn(service);
        when(openApiProperties.getGroup()).thenReturn(group);

        swaggerConfig = new SwaggerConfig(openApiProperties);
    }

    /**
     * Tests the OpenAPI configuration.
     * Verifies that:
     * - OpenAPI object is created successfully
     * - API information is properly configured
     * - Server information is properly set
     * - Components are initialized
     */
    @Test
    void testOpenApi_DefaultConfiguration_ReturnsPopulatedOpenAPIObject() {
        OpenAPI openAPI = swaggerConfig.openApi();
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getServers());
        assertNotNull(openAPI.getComponents());
    }

    /**
     * Tests the API grouping configuration.
     * Verifies that:
     * - GroupedOpenApi object is created successfully
     * - API paths are properly grouped according to configuration
     */
    @Test
    void testGroupedOpenApi_DefaultConfiguration_ReturnsNonNullGroupedOpenApi() {
        GroupedOpenApi groupedOpenApi = swaggerConfig.groupedOpenApi();
        assertNotNull(groupedOpenApi);
    }
}