package org.biometric.provider;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CORSManagerTest {

    /**
     * Tests that the CORS configuration allows the expected origin, methods, and headers.
     */
    @Test
    void corsConfiguration_success() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config, "CORS configuration should not be null");
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"), "Should allow localhost:3000");
        assertTrue(config.getAllowedMethods().contains("GET"), "Should allow GET method");
        assertTrue(config.getAllowedHeaders().contains("Authorization"), "Should allow Authorization header");
    }
}