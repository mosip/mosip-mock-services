package io.mosip.mock.mv.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MVExceptionTest {

    /**
     * Tests constructor with error code and message.
     * Verifies errorCode and errorText are set correctly.
     * Ensures cause is null when not provided.
     */
    @Test
    void constructorWithErrorCodeAndMessage_ValidatesMVException_Creation() {
        String errorCode = "ERR-001";
        String errorMessage = "Service failed";

        MVException exception = new MVException(errorCode, errorMessage);

        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorMessage, exception.getErrorText());
        assertNull(exception.getCause());
    }

    /**
     * Tests constructor with error code, message, and cause.
     * Verifies all three properties are set correctly.
     * Ensures cause is accessible via getCause().
     */
    @Test
    void constructorWithErrorCodeMessageAndCause_ValidatesMVException_Creation() {
        String errorCode = "ERR-002";
        String errorMessage = "Database failure";
        Throwable cause = new RuntimeException("Root cause");

        MVException exception = new MVException(errorCode, errorMessage, cause);

        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorMessage, exception.getErrorText());
        assertEquals(cause, exception.getCause());
    }
}