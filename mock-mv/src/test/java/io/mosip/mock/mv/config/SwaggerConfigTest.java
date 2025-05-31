package io.mosip.mock.mv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SwaggerConfig class to verify that the OpenAPI and GroupedOpenApi
 * beans are created correctly based on the provided OpenApiProperties.
 */
class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    /**
     * Initializes the SwaggerConfig instance with a fully populated OpenApiProperties object.
     * This simulates application configuration loading for Swagger/OpenAPI.
     */
    @BeforeEach
    void setUp() {
        // Setup LicenseProperty with name and URL
        LicenseProperty licenseProperty = new LicenseProperty();
        licenseProperty.setName("Apache 2.0");
        licenseProperty.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        // Setup InfoProperty with title, description, version, and license
        InfoProperty infoProperty = new InfoProperty();
        infoProperty.setTitle("Mock API");
        infoProperty.setDescription("Mock API Description");
        infoProperty.setVersion("1.0.0");
        infoProperty.setLicense(licenseProperty);

        // Setup custom server configuration (used internally, not Swagger's Server class)
        Server mosipServer1 = new Server();
        mosipServer1.setDescription("Localhost");
        mosipServer1.setUrl("http://localhost:8080");

        Server mosipServer2 = new Server();
        mosipServer2.setDescription("Production");
        mosipServer2.setUrl("https://api.example.com");

        Service service = new Service();
        service.setServers(List.of(mosipServer1, mosipServer2));

        // Setup Group with a name and list of API paths
        Group group = new Group();
        group.setName("mock-group");
        group.setPaths(List.of("/api/**", "/health"));

        // Combine all above into OpenApiProperties
        OpenApiProperties openApiProperties = new OpenApiProperties();
        openApiProperties.setInfo(infoProperty);
        openApiProperties.setService(service);
        openApiProperties.setGroup(group);

        // Initialize SwaggerConfig with the properties
        swaggerConfig = new SwaggerConfig(openApiProperties);
    }

    /**
     * Test to verify that the OpenAPI bean is created correctly and populated with expected metadata.
     */
    @Test
    void testOpenApiBeanCreation() {
        // Act: Create OpenAPI instance
        OpenAPI openAPI = swaggerConfig.openApi();
        Info info = openAPI.getInfo();

        // Assert: Validate OpenAPI Info values
        assertNotNull(openAPI);
        assertEquals("Mock API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("Mock API Description", info.getDescription());
        assertEquals("Apache 2.0", info.getLicense().getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", info.getLicense().getUrl());

        // Assert: Validate custom server info is mapped properly into OpenAPI servers
        assertNotNull(openAPI.getServers());
        assertEquals(2, openAPI.getServers().size());
        assertEquals("Localhost", openAPI.getServers().getFirst().getDescription());
        assertEquals("http://localhost:8080", openAPI.getServers().getFirst().getUrl());
    }

    /**
     * Test to verify that the GroupedOpenApi bean is created with the expected group name.
     */
    @Test
    void testGroupedOpenApiCreation() {
        // Act: Create GroupedOpenApi instance
        GroupedOpenApi groupedOpenApi = swaggerConfig.groupedOpenApi();

        // Assert: Validate group name is set correctly
        assertNotNull(groupedOpenApi);
        assertEquals("mock-group", groupedOpenApi.getGroup());
    }
}
