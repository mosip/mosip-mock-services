package io.mosip.proxy.abis.constant;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for the AbisErrorCode enum.
 * This class verifies the correctness of error codes, error messages,
 * and the behavior of utility methods in the AbisErrorCode enum.
 */
class AbisErrorCodeTest {

    /**
     * Test to verify that the error codes in the AbisErrorCode enum
     * match their expected values.
     */
    @Test
    void testErrorCodeValues() {
        assertEquals("MOS-MABIS-001", AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-002", AbisErrorCode.INVALID_KEY_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-003", AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-004", AbisErrorCode.INVALID_ID_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-005", AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-006", AbisErrorCode.QUEUE_CONNECTION_NOT_FOUND_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-007", AbisErrorCode.SET_EXPECTATION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-008", AbisErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-009", AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-010", AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-010", AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-011", AbisErrorCode.DATA_NULL_OR_EMPTY_EXCEPTION.getErrorCode());
        assertEquals("MOS-MABIS-500", AbisErrorCode.TECHNICAL_ERROR_EXCEPTION.getErrorCode());
    }

    /**
     * Test to verify that the error messages in the AbisErrorCode enum
     * match their expected values.
     */
    @Test
    void testErrorMessageValues() {
        assertEquals("Invalid connection", AbisErrorCode.INVALID_CONNECTION_EXCEPTION.getErrorMessage());
        assertEquals("Value does not exists for key", AbisErrorCode.INVALID_KEY_EXCEPTION.getErrorMessage());
        assertEquals("Data Decryption failure", AbisErrorCode.INVALID_DECRYPTION_EXCEPTION.getErrorMessage());
        assertEquals("Invalid Id value", AbisErrorCode.INVALID_ID_EXCEPTION.getErrorMessage());
        assertEquals("Value does not exists for key", AbisErrorCode.NO_VALUE_FOR_KEY_EXCEPTION.getErrorMessage());
        assertEquals("Queue Connection Not Found", AbisErrorCode.QUEUE_CONNECTION_NOT_FOUND_EXCEPTION.getErrorMessage());
        assertEquals("Invalid set expectation", AbisErrorCode.SET_EXPECTATION_EXCEPTION.getErrorMessage());
        assertEquals("Invalid get expectation", AbisErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage());
        assertEquals("Invalid delete expectation", AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage());
        assertEquals("Invalid configure", AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorMessage());
        assertEquals("Invalid cache:", AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorMessage());
        assertEquals("data is null or length is 0", AbisErrorCode.DATA_NULL_OR_EMPTY_EXCEPTION.getErrorMessage());
        assertEquals("Technical Error", AbisErrorCode.TECHNICAL_ERROR_EXCEPTION.getErrorMessage());
    }

    /**
     * Test to verify the behavior of the fromErrorCode method in the AbisErrorCode enum.
     * This method maps error codes to their corresponding enum values.
     */
    @Test
    void testFromErrorCode() {
        // Test valid error codes
        assertEquals(AbisErrorCode.INVALID_CONNECTION_EXCEPTION, AbisErrorCode.fromErrorCode("MOS-MABIS-001"));
        assertEquals(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION, AbisErrorCode.fromErrorCode("MOS-MABIS-003"));
        assertEquals(AbisErrorCode.TECHNICAL_ERROR_EXCEPTION, AbisErrorCode.fromErrorCode("MOS-MABIS-500"));

        // Test case insensitivity
        assertEquals(AbisErrorCode.INVALID_CONNECTION_EXCEPTION, AbisErrorCode.fromErrorCode("mos-mabis-001"));
        assertEquals(AbisErrorCode.INVALID_DECRYPTION_EXCEPTION, AbisErrorCode.fromErrorCode("mos-MABIS-003"));

        // Test invalid error codes
        assertEquals(AbisErrorCode.TECHNICAL_ERROR_EXCEPTION, AbisErrorCode.fromErrorCode("INVALID-CODE"));
        assertEquals(AbisErrorCode.TECHNICAL_ERROR_EXCEPTION, AbisErrorCode.fromErrorCode(""));
        assertEquals(AbisErrorCode.TECHNICAL_ERROR_EXCEPTION, AbisErrorCode.fromErrorCode(null));
    }

    /**
     * Test to verify the values method of the AbisErrorCode enum.
     * This method returns all the enum constants in the order they are declared.
     */
    @Test
    void testEnumValues() {
        AbisErrorCode[] errorCodes = AbisErrorCode.values();
        assertEquals(13, errorCodes.length);
        assertEquals(AbisErrorCode.INVALID_CONNECTION_EXCEPTION, errorCodes[0]);
        assertEquals(AbisErrorCode.TECHNICAL_ERROR_EXCEPTION, errorCodes[12]);
    }
}