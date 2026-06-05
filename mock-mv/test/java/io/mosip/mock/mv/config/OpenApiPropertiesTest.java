package io.mosip.mock.mv.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Unit test for OpenApiProperties and its nested classes to ensure that
 * all getters and setters work as expected.
 */
class OpenApiPropertiesTest {

    /**
     * Test method to validate setting and getting of properties for:
     * LicenseProperty, InfoProperty, Server, Service, Group, and OpenApiProperties.
     */
    @Test
    void openApiProperties_SetAndGet_success() {
        io.mosip.mock.mv.config.LicenseProperty license = new io.mosip.mock.mv.config.LicenseProperty();
        license.setName("MIT");
        license.setUrl("https://opensource.org/licenses/MIT");

        assertEquals("MIT", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());

        io.mosip.mock.mv.config.InfoProperty info = new io.mosip.mock.mv.config.InfoProperty();
        info.setTitle("Mock API");
        info.setDescription("Mock description");
        info.setVersion("v1.0");
        info.setLicense(license);

        assertEquals("Mock API", info.getTitle());
        assertEquals("Mock description", info.getDescription());
        assertEquals("v1.0", info.getVersion());
        assertEquals(license, info.getLicense());

        io.mosip.mock.mv.config.Server server1 = new io.mosip.mock.mv.config.Server();
        server1.setDescription("Local server");
        server1.setUrl("http://localhost:8080");

        io.mosip.mock.mv.config.Server server2 = new io.mosip.mock.mv.config.Server();
        server2.setDescription("Prod server");
        server2.setUrl("https://api.example.com");

        io.mosip.mock.mv.config.Service service = new io.mosip.mock.mv.config.Service();
        service.setServers(List.of(server1, server2));

        assertEquals(2, service.getServers().size());
        assertEquals("Local server", service.getServers().getFirst().getDescription());
        assertEquals("http://localhost:8080", service.getServers().getFirst().getUrl());

        io.mosip.mock.mv.config.Group group = new io.mosip.mock.mv.config.Group();
        group.setName("public");
        group.setPaths(List.of("/api/v1/**", "/health"));

        assertEquals("public", group.getName());
        assertEquals(2, group.getPaths().size());
        assertTrue(group.getPaths().contains("/api/v1/**"));

        io.mosip.mock.mv.config.OpenApiProperties openApiProperties = new io.mosip.mock.mv.config.OpenApiProperties();
        openApiProperties.setInfo(info);
        openApiProperties.setService(service);
        openApiProperties.setGroup(group);

        assertEquals(info, openApiProperties.getInfo());
        assertEquals(service, openApiProperties.getService());
        assertEquals(group, openApiProperties.getGroup());
    }
}