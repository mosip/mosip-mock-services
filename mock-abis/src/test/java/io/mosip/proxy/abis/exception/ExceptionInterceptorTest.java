package io.mosip.proxy.abis.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import io.mosip.proxy.abis.dto.FailureResponse;
import io.mosip.proxy.abis.dto.RequestMO;

import java.util.List;

/**
 * Unit test class for ExceptionInterceptor.
 * This class tests the exception handling methods of ExceptionInterceptor.
 */
class ExceptionInterceptorTest {

    private ExceptionInterceptor exceptionInterceptor;
    private RequestMO requestMO;

    /**
     * Sets up the test environment before each test.
     * Initializes the ExceptionInterceptor and a sample RequestMO object.
     */
    @BeforeEach
    void setUp() {
        exceptionInterceptor = new ExceptionInterceptor();
        requestMO = new RequestMO();
        requestMO.setId("test-id");
        requestMO.setRequestId("test-request-id");
        requestMO.setRequesttime(LocalDateTime.now());
    }

    /**
     * Tests the handleInsertRequestException method when a valid RequestException is passed.
     * Verifies the response status, body, and failure reason.
     */
    @Test
    void testExceptionInterceptor_HandleInsertRequestException_ReturnsErrorResponseWithCorrectDetails() {
        RequestException exception = new RequestException(requestMO, FailureReasonsConstants.INVALID_ID);
        ResponseEntity<Object> response = exceptionInterceptor.handleInsertRequestException(exception);
        assertNotNull(response); // Verify the response is not null
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Verify the status code
        assertNotNull(response.getBody()); // Verify the response body is not null
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals("test-id", failureResponse.getId()); // Verify the id in the response
        assertEquals("test-request-id", failureResponse.getRequestId()); // Verify the requestId in the response
        assertEquals(FailureReasonsConstants.INVALID_ID, failureResponse.getFailureReason()); // Verify the failure reason
    }

    /**
     * Tests the handleInsertRequestException method when a null RequestException is passed.
     * Verifies the response status and body.
     */
    @Test
    void testExceptionInterceptor_HandleInsertRequestExceptionWithNullEntity_ReturnsInternalServerErrorResponse() {
        RequestException exception = new RequestException(null, null);
        ResponseEntity<Object> response = exceptionInterceptor.handleInsertRequestException(exception);
        assertNotNull(response); // Verify the response is not null
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Verify the status code
        assertNotNull(response.getBody()); // Verify the response body is not null
    }

    /**
     * Tests the handleBindingErrors method when a valid BindingException is passed.
     * Verifies the response status, body, and failure reason.
     */
    @Test
    void testExceptionInterceptor_HandleBindingException_ReturnsErrorResponseWithFieldErrorDetails() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "error message");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.hasErrors()).thenReturn(true);
        BindingException exception = new BindingException(requestMO, bindingResult);
        ResponseEntity<Object> response = exceptionInterceptor.handleBindingErrors(exception);
        assertNotNull(response); // Verify the response is not null
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Verify the status code
        assertNotNull(response.getBody()); // Verify the response body is not null
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals("test-id", failureResponse.getId()); // Verify the id in the response
        assertEquals("test-request-id", failureResponse.getRequestId()); // Verify the requestId in the response
        assertNotNull(failureResponse.getFailureReason()); // Verify the failure reason is not null
    }

    /**
     * Tests the handleBindingErrors method when a null BindingException is passed.
     * Verifies the response status.
     */
    @Test
    void testExceptionInterceptor_HandleBindingExceptionWithNullEntity_ReturnsInternalServerErrorResponse() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(bindingResult.hasErrors()).thenReturn(false);

        BindingException exception = new BindingException(null, bindingResult);
        ResponseEntity<Object> response = exceptionInterceptor.handleBindingErrors(exception);

        assertNotNull(response); // Verify the response is not null
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Verify the status code
    }
}