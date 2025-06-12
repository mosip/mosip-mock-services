package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Test class for SBIResponseInfo
 * Tests HTTP response generation functionality including error responses,
 * success responses, and CORS headers
 */
class SBIResponseInfoTest {

    /**
     * Tests error response generation
     * Verifies that the generated error response contains:
     * - HTTP 405 status code
     * - Proper Content-Type header
     * - Error code and message in response body
     */
    @Test
    void generateErrorResponse_WithValidInputs_ReturnsFormattedErrorResponse() {
        String response = SBIResponseInfo.generateErrorResponse("eng", 8080, "ERR_001", "Test error");

        assertTrue(response.contains("HTTP/1.1 405 OK"));
        assertTrue(response.contains("Content-Type: application/json"));
        assertTrue(response.contains("Test error"));
        assertTrue(response.contains("ERR_001"));
    }

    /**
     * Tests admin API error response generation
     * Verifies that the generated admin error response contains:
     * - HTTP 405 status code
     * - Proper Content-Type header
     * - Admin-specific error code and message
     */
    @Test
    void generateAdminApiErrorResponse_WithValidInputs_ReturnsFormattedAdminErrorResponse() {
        String response = SBIResponseInfo.generateAdminApiErrorResponse("eng", 8080, "ADMIN_ERR_001", "Admin error");

        assertTrue(response.contains("HTTP/1.1 405 OK"));
        assertTrue(response.contains("Content-Type: application/json"));
        assertTrue(response.contains("Admin error"));
        assertTrue(response.contains("ADMIN_ERR_001"));
    }

    /**
     * Tests successful response generation
     * Verifies that the generated success response contains:
     * - HTTP 200 status code
     * - Proper Content-Type header
     * - Correct Content-Length header
     * - JSON response body
     */
    @Test
    void generateResponse_WithValidJson_ReturnsFormattedHttpResponse() {
        String jsonResponse = "{\"status\":\"success\"}";
        String response = SBIResponseInfo.generateResponse("eng", 8080, jsonResponse);

        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: application/json"));
        assertTrue(response.contains(jsonResponse));
        assertTrue(response.contains("Content-Length: " + jsonResponse.length()));
    }

    /**
     * Tests OPTIONS response generation
     * Verifies that the generated OPTIONS response contains:
     * - HTTP 200 status code
     * - Required CORS headers
     * - Cache control headers
     * - Connection keep-alive header
     */
    @Test
    void generateOptionsResponse_ReturnsExpectedHttpOptionsResponse() {
        String response = SBIResponseInfo.generateOptionsResponse();

        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Access-Control-Allow-Origin:*"));
        assertTrue(response.contains("Access-Control-Allow-Headers:Content-Type"));
        assertTrue(response.contains("CACHE-CONTROL:no-cache"));
        assertTrue(response.contains("Connection: Keep-Alive"));
    }

    /**
     * Tests private constructor behavior
     * Verifies that:
     * - Constructor is private
     * - Attempting to instantiate throws IllegalStateException
     * - Exception message is correct
     */
    @Test
    void constructor_InvokingPrivateConstructor_ThrowsIllegalStateException() throws Exception {
        Constructor<SBIResponseInfo> constructor = SBIResponseInfo.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
            fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("SBIResponseInfo class", e.getCause().getMessage());
        }
    }

    /**
     * Tests response generation with null content
     * Verifies that:
     * - Response is generated without errors
     * - HTTP 200 status is included
     * - Content-Length header is omitted
     */
    @Test
    void generateResponse_WithNullContent_ReturnsHttpResponseWithoutContentLength() {
        String response = SBIResponseInfo.generateResponse("eng", 8080, null);

        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertFalse(response.contains("Content-Length:"));
    }

    /**
     * Tests CORS headers inclusion
     * Verifies that all required CORS headers are present:
     * - Access-Control-Allow-Origin
     * - Access-Control-Allow-Headers
     * - Access-Control-Allow-Methods
     * - Access-Control-Allow-Credentials
     */
    @Test
    void responseContainsCORSHeaders_WithValidResponse_ReturnsExpectedCORSHeaders() {
        String response = SBIResponseInfo.generateResponse("eng", 8080, "test");

        assertTrue(response.contains("Access-Control-Allow-Origin: *"));
        assertTrue(response.contains("Access-Control-Allow-Headers"));
        assertTrue(response.contains("Access-Control-Allow-Methods"));
        assertTrue(response.contains("Access-Control-Allow-Credentials: true"));
    }
}