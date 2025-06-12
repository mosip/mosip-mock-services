package io.mosip.mock.sdk.exceptions;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Test class for SDKException to verify custom exception handling behavior
 */
@RunWith(MockitoJUnitRunner.class)
class SDKExceptionTest {

    private static final String ERROR_CODE = "ERR-001";
    private static final String ERROR_MESSAGE = "Test error message";

    /**
     * Tests the constructor that takes error code and message parameters
     * Verifies that the exception correctly stores and returns these values
     */
    @Test
    void constructorWithCodeAndMessage_StoresValuesCorrectly() {
        SDKException exception = new SDKException(ERROR_CODE, ERROR_MESSAGE);

        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertEquals(ERROR_CODE, exception.getErrorCode());
        assertEquals(ERROR_MESSAGE, exception.getErrorText());
    }

    /**
     * Tests that SDKException properly extends BaseUncheckedException
     * Verifies the inheritance hierarchy is correct
     */
    @Test
    void inheritance_ValidInstance_ChecksBaseClass() {
        SDKException exception = new SDKException(ERROR_CODE, ERROR_MESSAGE);
        assertTrue(exception instanceof BaseUncheckedException);
    }

    /**
     * Tests handling of null values passed to the constructor
     * Verifies that null values are properly handled without throwing errors
     */
    @Test
    public void nullValues_NullParameters_ReturnsNullFields() {
        SDKException exception = new SDKException(null, null);

        assertNull(exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorText());
    }
}