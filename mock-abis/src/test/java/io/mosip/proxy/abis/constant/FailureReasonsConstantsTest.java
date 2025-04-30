package io.mosip.proxy.abis.constant;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Unit test class for the FailureReasonsConstants class.
 * This class verifies the correctness of constant values and ensures
 * that the private constructor behaves as expected.
 */
class FailureReasonsConstantsTest {

    /**
     * Test to verify that the private constructor of the FailureReasonsConstants class
     * throws an IllegalStateException when invoked via reflection.
     *
     * @throws Exception if reflection fails to access the constructor
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<FailureReasonsConstants> constructor = FailureReasonsConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Should have thrown IllegalStateException");
        } catch (InvocationTargetException e) {
            // Verify that the cause of the exception is an IllegalStateException
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("FailureReasonsConstants class", e.getCause().getMessage());
        }
    }

    /**
     * Test to verify that the constant values in the FailureReasonsConstants class
     * match their expected values.
     */
    @Test
    void testConstantValues() {
        assertEquals("1", FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
        assertEquals("2", FailureReasonsConstants.ABORTED);
        assertEquals("3", FailureReasonsConstants.UNEXPECTED_ERROR);
        assertEquals("4", FailureReasonsConstants.UNABLE_TO_SERVE_THE_REQUEST_INVALID_REQUEST_STRUCTURE);
        assertEquals("5", FailureReasonsConstants.MISSING_REFERENCEID);
        assertEquals("6", FailureReasonsConstants.MISSING_REQUESTID);
        assertEquals("7", FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
        assertEquals("8", FailureReasonsConstants.MISSING_REFERENCE_URL);
        assertEquals("9", FailureReasonsConstants.MISSING_REQUESTTIME);
        assertEquals("10", FailureReasonsConstants.REFERENCEID_ALREADY_EXISTS);
        assertEquals("11", FailureReasonsConstants.CBEFF_HAS_NO_DATA);
        assertEquals("12", FailureReasonsConstants.REFERENCEID_NOT_FOUND);
        assertEquals("13", FailureReasonsConstants.INVALID_VERSION);
        assertEquals("14", FailureReasonsConstants.INVALID_ID);
        assertEquals("15", FailureReasonsConstants.INVALID_REQUESTTIME_FORMAT);
        assertEquals("16", FailureReasonsConstants.INVALID_CBEFF_FORMAT);
        assertEquals("17", FailureReasonsConstants.DATA_SHARE_URL_EXPIRED);
        assertEquals("18", FailureReasonsConstants.BIOMETRIC_QUALITY_CHECK_FAILED);
    }
}