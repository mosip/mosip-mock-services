package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for RequestMO.
 * This class tests the functionality of the RequestMO class, including its constructors,
 * getters, and setters.
 */
class RequestMOTest {

    /**
     * Tests the parameterized constructor of RequestMO.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        RequestMO request = new RequestMO("id1", "1.0", "req123", now, "refId1");

        assertEquals("id1", request.getId());
        assertEquals("1.0", request.getVersion());
        assertEquals("req123", request.getRequestId());
        assertEquals(now, request.getRequesttime());
        assertEquals("refId1", request.getReferenceId());
    }

    /**
     * Tests the default constructor of RequestMO.
     * Verifies that all fields are initialized to their default values (null).
     */
    @Test
    void testDefaultConstructor() {
        RequestMO request = new RequestMO();

        assertNull(request.getId());
        assertNull(request.getVersion());
        assertNull(request.getRequestId());
        assertNull(request.getRequesttime());
        assertNull(request.getReferenceId());
    }

    /**
     * Tests the setters and getters of RequestMO.
     * Verifies that fields can be set and retrieved correctly.
     */
    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        RequestMO request = new RequestMO();

        request.setId("id1");
        request.setVersion("1.0");
        request.setRequestId("req123");
        request.setRequesttime(now);
        request.setReferenceId("refId1");

        assertEquals("id1", request.getId());
        assertEquals("1.0", request.getVersion());
        assertEquals("req123", request.getRequestId());
        assertEquals(now, request.getRequesttime());
        assertEquals("refId1", request.getReferenceId());
    }
}