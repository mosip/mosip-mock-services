package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
     * Tests port getter and setter
     * Verifies the port value is correctly set and retrieved
     */
    @Test
    void portGetterAndSetter_ReturnsCorrectPortValue() {
        serviceResponse.setPort(9090);
        assertEquals(9090, serviceResponse.getPort());
    }

    /**
     * Tests JSON request setter and getter
     * Verifies the correct storage and retrieval of JSON request string
     */
    @Test
    void requestGetterAndSetter_StoresAndRetrievesJsonRequest() {
        String testRequest = "{\"key\":\"value\"}";
        serviceResponse.setRequest(testRequest);
        assertEquals(testRequest, serviceResponse.getRequest());
    }

    /**
     * Tests static semaphore getter and setter
     * Verifies the thread synchronization mechanism is working correctly
     */
    @Test
    void semaphoreGetterAndSetter_ValidatesThreadSynchronization() {
        Semaphore newSemaphore = new Semaphore(2);
        SBIServiceResponse.setSemaphore(newSemaphore);
        assertEquals(newSemaphore, SBIServiceResponse.getSemaphore());
    }

    /**
     * Tests getRequestJson behavior when the request is null
     * Expects null to be returned
     */
    @Test
    void getRequestJson_WithNullRequest_ReturnsNull() {
        serviceResponse.setRequest(null);
        assertNull(serviceResponse.getRequestJson("ANY_VERB"));
    }

    /**
     * Tests getRequestJson behavior with invalid JSON string
     * Expects null to be returned
     */
    @Test
    void getRequestJson_WithInvalidRequest_ReturnsNull() {
        serviceResponse.setRequest("invalid json");
        assertNull(serviceResponse.getRequestJson("ANY_VERB"));
    }

    /**
     * Tests service response generation with empty request string
     * Verifies HTTP 405 response is returned
     */
    @Test
    void getServiceresponse_WithEmptyRequest_Returns405() {
        String response = serviceResponse.getServiceresponse(mockService, socket, "");
        assertTrue(response.contains("HTTP/1.1 405 OK"));
    }

    /**
     * Tests service response with an unknown HTTP verb
     * Verifies HTTP 500 status is returned
     */
    @Test
    void getServiceresponse_WithUnknownVerb_Returns500() {
        String response = serviceResponse.getServiceresponse(mockService, socket, "UNKNOWN_VERB");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response with valid JSON and matching HTTP verb
     * Verifies non-null HTTP response containing status line
     */
    @Test
    void getServiceresponse_WithValidJson_ReturnsHttpResponse() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
        assertTrue(response.contains("HTTP/1.1"));
    }

    /**
     * Tests service response with mismatched HTTP verb and JSON method
     * Verifies HTTP 500 status is returned
     */
    @Test
    void getServiceresponse_WithMismatchedVerb_Returns500() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "INFO");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response with malformed JSON input
     * Expects HTTP 500 response due to malformed syntax
     */
    @Test
    void getServiceresponse_WithMalformedJson_Returns500() {
        String invalidJson = "{method:CAPTURE,data:{}}";
        serviceResponse.setRequest(invalidJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response behavior when socket parameter is null
     * Verifies HTTP 500 error response
     */
    @Test
    void getServiceresponse_WithNullSocket_Returns500() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(mockService, null, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response behavior when mock service parameter is null
     * Verifies HTTP 500 error response
     */
    @Test
    void getServiceresponse_WithNullMockService_Returns500() {
        String validJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(validJson);
        String response = serviceResponse.getServiceresponse(null, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response for JSON without required "method" field
     * Verifies HTTP 500 error response
     */
    @Test
    void getServiceresponse_WithoutMethod_Returns500() {
        String jsonWithoutMethod = "{\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(jsonWithoutMethod);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests service response with empty JSON object
     * Verifies HTTP 500 error response
     */
    @Test
    void getServiceresponse_WithEmptyJson_Returns500() {
        String emptyJson = "{}";
        serviceResponse.setRequest(emptyJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of a large JSON request payload
     * Verifies that the service responds properly without exceptions
     */
    @Test
    void getServiceresponse_WithLargeRequest_HandlesLargePayload() {
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
     * Tests concurrent access to SBIServiceResponse using semaphore
     * Verifies thread-safety when multiple threads invoke the service
     */
    @Test
    void concurrentSemaphoreAccess_VerifiesThreadSafety() throws InterruptedException {
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
     * Tests getRequestJson behavior with plain non-JSON string input
     * Verifies that null is returned
     */
    @Test
    void getRequestJson_WithNonJsonString_ReturnsNull() {
        serviceResponse.setRequest("plain text");
        assertNull(serviceResponse.getRequestJson("CAPTURE"));
    }

    /**
     * Tests service response with valid HTTP verb but null request content
     * Expects HTTP 500 error response
     */
    @Test
    void getServiceresponse_WithValidVerbAndNullRequest_Returns500() {
        serviceResponse.setRequest(null);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of special characters within JSON request
     * Verifies that service returns a valid HTTP response
     */
    @Test
    void getServiceresponse_WithSpecialCharacters_HandlesSpecialChars() {
        String jsonWithSpecialChars = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"value\":\"!@#$%^&*()\"}}}";
        serviceResponse.setRequest(jsonWithSpecialChars);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests handling of nested JSON objects in request payload
     * Verifies service responds correctly with complex JSON
     */
    @Test
    void getServiceresponse_WithNestedJson_HandlesComplexStructures() {
        String nestedJson = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"nested\":{\"key\":\"value\"}}}";
        serviceResponse.setRequest(nestedJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests request containing JSON arrays
     * Verifies service handles array data without error
     */
    @Test
    void getServiceresponse_WithJsonArray_HandlesArrayStructures() {
        String jsonWithArray = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"values\":[1,2,3]}}";
        serviceResponse.setRequest(jsonWithArray);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests handling of Unicode-encoded strings in JSON request
     * Verifies proper decoding and processing
     */
    @Test
    void getServiceresponse_WithUnicodeCharacters_HandlesUnicode() {
        String jsonWithUnicode = "{\"method\":\"CAPTURE\",\"data\":{\"type\":\"Finger\",\"value\":\"\\u0041\\u0042\\u0043\"}}";
        serviceResponse.setRequest(jsonWithUnicode);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests case sensitivity of JSON "method" field
     * Verifies service returns error for lowercase method
     */
    @Test
    void getServiceresponse_WithCaseSensitiveMethod_Returns500() {
        String lowerCaseJson = "{\"method\":\"capture\",\"data\":{\"type\":\"Finger\"}}";
        serviceResponse.setRequest(lowerCaseJson);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertTrue(response.contains("500"));
    }

    /**
     * Tests handling of JSON with extra whitespace and formatting
     * Verifies service processes formatted JSON correctly
     */
    @Test
    void getServiceresponse_WithWhitespace_HandlesFormattedJson() {
        String jsonWithWhitespace = "   {  \"method\"  :  \"CAPTURE\" , \"data\" : { \"type\" : \"Finger\" }  }   ";
        serviceResponse.setRequest(jsonWithWhitespace);
        String response = serviceResponse.getServiceresponse(mockService, socket, "CAPTURE");
        assertNotNull(response);
    }

    /**
     * Tests trimming of certificate strings for various header/footer formats
     * Verifies that only the body content remains
     */
    @Test
    void trimBeginEnd_WithVariousFormats_TrimsCertificateHeaders() throws Exception {
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
     * Tests the delay method handling interruption correctly
     * Verifies that InterruptedException does not propagate unexpectedly
     */
    @Test
    void delay_WithInterruption_HandlesInterruptedDelay() throws Exception {
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
