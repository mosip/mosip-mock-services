package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for ResponseMO.
 * This class tests the functionality of the ResponseMO class, including its constructors,
 * getters, and setters.
 */
class ResponseMOTest {

    /**
     * Tests the parameterized constructor of ResponseMO.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ResponseMO response = new ResponseMO("id1", "req123", now, "Success");

        assertEquals("id1", response.getId());
        assertEquals("req123", response.getRequestId());
        assertEquals(now, response.getResponsetime());
        assertEquals("Success", response.getReturnValue());
    }

    /**
     * Tests the default constructor of ResponseMO.
     * Verifies that all fields are initialized to their default values (null).
     */
    @Test
    void testDefaultConstructor() {
        ResponseMO response = new ResponseMO();

        assertNull(response.getId());
        assertNull(response.getRequestId());
        assertNull(response.getResponsetime());
        assertNull(response.getReturnValue());
    }

    /**
     * Tests the setters and getters of ResponseMO.
     * Verifies that fields can be set and retrieved correctly.
     */
    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        ResponseMO response = new ResponseMO();

        response.setId("id1");
        response.setRequestId("req123");
        response.setResponsetime(now);
        response.setReturnValue("Success");

        assertEquals("id1", response.getId());
        assertEquals("req123", response.getRequestId());
        assertEquals(now, response.getResponsetime());
        assertEquals("Success", response.getReturnValue());
    }
}