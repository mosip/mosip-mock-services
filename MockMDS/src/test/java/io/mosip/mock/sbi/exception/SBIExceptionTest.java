package io.mosip.mock.sbi.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for SBIException
 * Tests the creation and inheritance behavior of the custom SBI exception class
 */
class SBIExceptionTest {

    /**
     * Tests SBIException creation with error code, message and root cause
     * Verifies:
     * 1. Exception message is set correctly
     * 2. Root cause is properly linked
     */
    @Test
    void testSBIExceptionCreation() {
        String errorCode = "ERR_001";
        String errorMessage = "Test error message";
        Exception rootCause = new RuntimeException("Root cause");

        SBIException exception = new SBIException(errorCode, errorMessage, rootCause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(rootCause, exception.getCause());
    }

    /**
     * Tests if SBIException properly extends Exception class
     * Verifies that SBIException is an instance of Exception class
     */
    @Test
    void testSBIExceptionInheritance() {
        SBIException exception = new SBIException("ERR_002", "Test message", null);
        assertTrue(exception instanceof Exception);
    }
}