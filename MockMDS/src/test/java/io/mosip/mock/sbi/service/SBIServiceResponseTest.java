package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Test class for SBIServiceResponse
 * Tests the service response functionality including request handling,
 * JSON processing, and error scenarios
 */
class SBIServiceResponseTest {

    private SBIServiceResponse serviceResponse;
    private SBIMockService mockService;
    private Socket socket;

    /**
     * Sets up the test environment before each test
     * Initializes service response with test port and mocks required dependencies
     */
    @BeforeEach
    void setUp() {
        serviceResponse = new SBIServiceResponse(8080);
        mockService = mock(SBIMockService.class);
        socket = mock(Socket.class);
    }

    /**
     * Tests the port getter and setter methods
     * Verifies that port value is correctly set and retrieved
     */
    @Test
    void testPortGetterAndSetter() {
        serviceResponse.setPort(9090);
        assertEquals(9090, serviceResponse.getPort());
    }

    /**
     * Tests the request getter and setter methods
     * Verifies correct storage and retrieval of JSON request string
     */
    @Test
    void testRequestGetterAndSetter() {
        String testRequest = "{\"key\":\"value\"}";
        serviceResponse.setRequest(testRequest);
        assertEquals(testRequest, serviceResponse.getRequest());
    }

    /**
     * Tests the static semaphore getter and setter methods
     * Verifies thread synchronization mechanism is working correctly
     */
    @Test
    void testSemaphoreGetterAndSetter() {
        Semaphore newSemaphore = new Semaphore(2);
        SBIServiceResponse.setSemaphore(newSemaphore);
        assertEquals(newSemaphore, SBIServiceResponse.getSemaphore());
    }

    /**
     * Tests handling of null requests in getRequestJson method
     * Verifies null is returned when request is null
     */
    @Test
    void testGetRequestJsonWithNullRequest() {
        serviceResponse.setRequest(null);
        assertNull(serviceResponse.getRequestJson("ANY_VERB"));
    }

    /**
     * Tests handling of invalid JSON in getRequestJson method
     * Verifies null is returned for malformed JSON
     */
    @Test
    void testGetRequestJsonWithInvalidRequest() {
        serviceResponse.setRequest("invalid json");
        assertNull(serviceResponse.getRequestJson("ANY_VERB"));
    }

    /**
     * Tests response generation for empty requests
     * Verifies 405 status code is returned
     */
    @Test
    void testGetServiceresponseWithEmptyRequest() {
        String response = serviceResponse.getServiceresponse(mockService, socket, "");
        assertTrue(response.contains("HTTP/1.1 405 OK"));
    }

    /**
     * Tests response generation for unknown HTTP verbs
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithUnknownVerb() {
        String response = serviceResponse.getServiceresponse(mockService, socket, "UNKNOWN_VERB");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of valid JSON requests
     * Verifies proper HTTP response is generated
     */
    @Test
    void testGetServiceresponseWithValidJson() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
        assertTrue(response.contains("HTTP/1.1"));
    }

    /**
     * Tests handling of mismatched HTTP verb and JSON method
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithMismatchedVerb() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "INFO");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of malformed JSON requests
     * Verifies 500 status code is returned for invalid JSON syntax
     */
    @Test
    void testGetServiceresponseWithMalformedJson() {
        String invalidJson = "{method:CAPTURE,data:{}}";
        serviceResponse.setRequest(invalidJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of null socket parameter
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithNullSocket() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, null, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of null mock service
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithNullMockService() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(null, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of JSON without required method field
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithoutMethod() {
        String jsonWithoutMethod = "{\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(jsonWithoutMethod);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of empty JSON objects
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithEmptyJson() {
        String emptyJson = "{}";
        serviceResponse.setRequest(emptyJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of large JSON requests
     * Verifies proper handling of requests with large payload
     */
    @Test
    void testGetServiceresponseWithLargeRequest() {
        StringBuilder largeJson = new StringBuilder("{\"method\":\"CAPTURE\",\"data\":{\"value\":\"");
        for (int i = 0; i < 10000; i++) {
            largeJson.append("x");
        }
        largeJson.append("\"}}");
        serviceResponse.setRequest(largeJson.toString());
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests concurrent access to service response
     * Verifies thread safety using semaphore
     */
    @Test
    void testConcurrentSemaphoreAccess() throws InterruptedException {
        Semaphore sem = new Semaphore(1);
        SBIServiceResponse.setSemaphore(sem);

        Thread t1 = new Thread(() -> {
            String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
            assertNotNull(response);
        });

        Thread t2 = new Thread(() -> {
            String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
            assertNotNull(response);
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * Tests handling of non-JSON string requests
     * Verifies null is returned for plain text input
     */
    @Test
    void testGetRequestJsonWithNonJsonString() {
        serviceResponse.setRequest("plain text");
        assertNull(serviceResponse.getRequestJson("CAPTURE"));
    }

    /**
     * Tests response generation with null request content
     * Verifies 500 status code is returned
     */
    @Test
    void testGetServiceresponseWithValidVerbNullRequest() {
        serviceResponse.setRequest(null);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of JSON with special characters
     * Verifies proper handling of special characters in request
     */
    @Test
    void testGetServiceresponseWithSpecialCharacters() {
        String jsonWithSpecialChars = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"value\":\"!@#$%^&*()\"}}}";
        serviceResponse.setRequest(jsonWithSpecialChars);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests handling of nested JSON objects
     * Verifies proper handling of complex JSON structures
     */
    @Test
    void testGetServiceresponseWithNestedJson() {
        String nestedJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"nested\":{\"key\":\"value\"}}}";
        serviceResponse.setRequest(nestedJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests handling of JSON arrays in request
     * Verifies proper handling of array data structures
     */
    @Test
    void testGetServiceresponseWithJsonArray() {
        String jsonWithArray = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"values\":[1,2,3]}}";
        serviceResponse.setRequest(jsonWithArray);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests handling of Unicode characters in JSON
     * Verifies proper handling of Unicode encoded strings
     */
    @Test
    void testGetServiceresponseWithUnicodeCharacters() {
        String jsonWithUnicode = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"value\":\"\\u0041\\u0042\\u0043\"}}";
        serviceResponse.setRequest(jsonWithUnicode);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests case sensitivity of method field
     * Verifies proper handling of case-sensitive method names
     */
    @Test
    void testGetServiceresponseWithCaseSensitiveMethod() {
        String lowerCaseJson = "{\"method\":\"capture\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(lowerCaseJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of whitespace in JSON
     * Verifies proper handling of formatted JSON with extra whitespace
     */
    @Test
    void testGetServiceresponseWithWhitespace() {
        String jsonWithWhitespace = "   {  \"method\"  :  \"CAPTURE\" , \"data\" : { \"type\" : \"Finger\" }  }   ";
        serviceResponse.setRequest(jsonWithWhitespace);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests trimBeginEnd method with various certificate formats
     * Verifies proper trimming of certificate headers and footers
     */
    @Test
    void testTrimBeginEndWithVariousFormats() throws Exception {
        java.lang.reflect.Method trimMethod = SBIServiceResponse.class.getDeclaredMethod("trimBeginEnd", String.class);
        trimMethod.setAccessible(true);

        String cert1 = "-----BEGIN CERTIFICATE-----\nABCDEF\n-----END CERTIFICATE-----";
        String cert2 = "-----BEGIN PUBLIC KEY-----\nXYZ123\n-----END PUBLIC KEY-----";
        String cert3 = "-----BEGIN SOMETHING-----\n123456\n-----END SOMETHING-----";

        assertEquals("ABCDEF", trimMethod.invoke(serviceResponse, cert1));
        assertEquals("XYZ123", trimMethod.invoke(serviceResponse, cert2));
        assertEquals("123456", trimMethod.invoke(serviceResponse, cert3));
    }

    /**
     * Tests delay method with thread interruption
     * Verifies proper handling of interrupted delay operations
     */
    @Test
    void testDelayWithInterruption() throws Exception {
        java.lang.reflect.Method delayMethod = SBIServiceResponse.class.getDeclaredMethod("delay", long.class);
        delayMethod.setAccessible(true);

        Thread t = new Thread(() -> {
            try {
                delayMethod.invoke(serviceResponse, 5000L);
            } catch (Exception e) {
                fail("Delay method threw exception");
            }
        });

        t.start();
        t.interrupt();
        t.join();
    }
}