package io.mosip.mock.mv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the SwaggerConfig class to verify that the OpenAPI and GroupedOpenApi
 * beans are created correctly based on the provided OpenApiProperties.
 */
class SwaggerConfigTest {

    private io.mosip.mock.mv.config.SwaggerConfig swaggerConfig;

    /**
     * Initializes the SwaggerConfig instance with a fully populated OpenApiProperties object.
     * This simulates application configuration loading for Swagger/OpenAPI.
     */
    @BeforeEach
    void setUp() {
        io.mosip.mock.mv.config.LicenseProperty licenseProperty = new io.mosip.mock.mv.config.LicenseProperty();
        licenseProperty.setName("Apache 2.0");
        licenseProperty.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        io.mosip.mock.mv.config.InfoProperty infoProperty = new io.mosip.mock.mv.config.InfoProperty();
        infoProperty.setTitle("Mock API");
        infoProperty.setDescription("Mock API Description");
        infoProperty.setVersion("1.0.0");
        infoProperty.setLicense(licenseProperty);

        io.mosip.mock.mv.config.Server mosipServer1 = new io.mosip.mock.mv.config.Server();
        mosipServer1.setDescription("Localhost");
        mosipServer1.setUrl("http://localhost:8080");

        io.mosip.mock.mv.config.Server mosipServer2 = new io.mosip.mock.mv.config.Server();
        mosipServer2.setDescription("Production");
        mosipServer2.setUrl("https://api.example.com");

        io.mosip.mock.mv.config.Service service = new io.mosip.mock.mv.config.Service();
        service.setServers(List.of(mosipServer1, mosipServer2));

        io.mosip.mock.mv.config.Group group = new io.mosip.mock.mv.config.Group();
        group.setName("mock-group");
        group.setPaths(List.of("/api/**", "/health"));

        io.mosip.mock.mv.config.OpenApiProperties openApiProperties = new io.mosip.mock.mv.config.OpenApiProperties();
        openApiProperties.setInfo(infoProperty);
        openApiProperties.setService(service);
        openApiProperties.setGroup(group);

        swaggerConfig = new io.mosip.mock.mv.config.SwaggerConfig(openApiProperties);
    }

    /**
     * Test to verify that the OpenAPI bean is created correctly and populated with expected metadata.
     */
    @Test
    void openApiBeanCreation_ValidatesOpenAPIInstance_success() {
        OpenAPI openAPI = swaggerConfig.openApi();
        Info info = openAPI.getInfo();

        assertNotNull(openAPI);
        assertEquals("Mock API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("Mock API Description", info.getDescription());
        assertEquals("Apache 2.0", info.getLicense().getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", info.getLicense().getUrl());

        assertNotNull(openAPI.getServers());
        assertEquals(2, openAPI.getServers().size());
        assertEquals("Localhost", openAPI.getServers().getFirst().getDescription());
        assertEquals("http://localhost:8080", openAPI.getServers().getFirst().getUrl());
    }

    /**
     * Test to verify that the GroupedOpenApi bean is created with the expected group name.
     */
    @Test
    void groupedOpenApiCreation_ValidatesGroupName_success() {
        GroupedOpenApi groupedOpenApi = swaggerConfig.groupedOpenApi();

        assertNotNull(groupedOpenApi);
        assertEquals("mock-group", groupedOpenApi.getGroup());
    }
}