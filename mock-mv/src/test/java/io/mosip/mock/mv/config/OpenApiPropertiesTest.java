package io.mosip.mock.mv.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    void testOpenApiPropertiesSetAndGet() {
        // Create and test LicenseProperty
        LicenseProperty license = new LicenseProperty();
        license.setName("MIT");
        license.setUrl("https://opensource.org/licenses/MIT");

        // Verify LicenseProperty values
        assertEquals("MIT", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());

        // Create and test InfoProperty
        InfoProperty info = new InfoProperty();
        info.setTitle("Mock API");
        info.setDescription("Mock description");
        info.setVersion("v1.0");
        info.setLicense(license);

        // Verify InfoProperty values
        assertEquals("Mock API", info.getTitle());
        assertEquals("Mock description", info.getDescription());
        assertEquals("v1.0", info.getVersion());
        assertEquals(license, info.getLicense());

        // Create and test Server instances
        Server server1 = new Server();
        server1.setDescription("Local server");
        server1.setUrl("http://localhost:8080");

        Server server2 = new Server();
        server2.setDescription("Prod server");
        server2.setUrl("https://api.example.com");

        // Create and test Service with server list
        Service service = new Service();
        service.setServers(List.of(server1, server2));

        // Verify Service and Server values
        assertEquals(2, service.getServers().size());
        assertEquals("Local server", service.getServers().getFirst().getDescription());
        assertEquals("http://localhost:8080", service.getServers().getFirst().getUrl());

        // Create and test Group
        Group group = new Group();
        group.setName("public");
        group.setPaths(List.of("/api/v1/**", "/health"));

        // Verify Group values
        assertEquals("public", group.getName());
        assertEquals(2, group.getPaths().size());
        assertTrue(group.getPaths().contains("/api/v1/**"));

        // Create OpenApiProperties and set nested objects
        OpenApiProperties openApiProperties = new OpenApiProperties();
        openApiProperties.setInfo(info);
        openApiProperties.setService(service);
        openApiProperties.setGroup(group);

        // Verify OpenApiProperties values
        assertEquals(info, openApiProperties.getInfo());
        assertEquals(service, openApiProperties.getService());
        assertEquals(group, openApiProperties.getGroup());
    }
}
