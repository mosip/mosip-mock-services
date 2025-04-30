package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for MockAbisQueueDetails.
 * This class tests the functionality of the MockAbisQueueDetails class, including its constructors,
 * getters, and setters.
 */
class MockAbisQueueDetailsTest {

    /**
     * Tests the parameterized constructor of MockAbisQueueDetails.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        MockAbisQueueDetails details = new MockAbisQueueDetails(
                "Queue1",
                "localhost",
                "8080",
                "tcp://localhost:61616",
                "inboundQueue",
                "outboundQueue",
                "pingInboundQueue",
                "pingOutboundQueue",
                "user",
                "password",
                "mock"
        );

        assertEquals("Queue1", details.getName());
        assertEquals("localhost", details.getHost());
        assertEquals("8080", details.getPort());
        assertEquals("tcp://localhost:61616", details.getBrokerUrl());
        assertEquals("inboundQueue", details.getInboundQueueName());
        assertEquals("outboundQueue", details.getOutboundQueueName());
        assertEquals("pingInboundQueue", details.getPingInboundQueueName());
        assertEquals("pingOutboundQueue", details.getPingOutboundQueueName());
        assertEquals("user", details.getUserName());
        assertEquals("password", details.getPassword());
        assertEquals("mock", details.getTypeOfQueue());
    }

    /**
     * Tests the default constructor of MockAbisQueueDetails.
     * Verifies that all fields are initialized to their default values (null).
     */
    @Test
    void testDefaultConstructor() {
        MockAbisQueueDetails details = new MockAbisQueueDetails();

        assertNull(details.getName());
        assertNull(details.getHost());
        assertNull(details.getPort());
        assertNull(details.getBrokerUrl());
        assertNull(details.getInboundQueueName());
        assertNull(details.getOutboundQueueName());
        assertNull(details.getPingInboundQueueName());
        assertNull(details.getPingOutboundQueueName());
        assertNull(details.getUserName());
        assertNull(details.getPassword());
        assertNull(details.getTypeOfQueue());
    }

    /**
     * Tests the setters and getters of MockAbisQueueDetails.
     * Verifies that fields can be set and retrieved correctly.
     */
    @Test
    void testSettersAndGetters() {
        MockAbisQueueDetails details = new MockAbisQueueDetails();

        details.setName("Queue1");
        details.setHost("localhost");
        details.setPort("8080");
        details.setBrokerUrl("tcp://localhost:61616");
        details.setInboundQueueName("inboundQueue");
        details.setOutboundQueueName("outboundQueue");
        details.setPingInboundQueueName("pingInboundQueue");
        details.setPingOutboundQueueName("pingOutboundQueue");
        details.setUserName("user");
        details.setPassword("password");
        details.setTypeOfQueue("mock");

        assertEquals("Queue1", details.getName());
        assertEquals("localhost", details.getHost());
        assertEquals("8080", details.getPort());
        assertEquals("tcp://localhost:61616", details.getBrokerUrl());
        assertEquals("inboundQueue", details.getInboundQueueName());
        assertEquals("outboundQueue", details.getOutboundQueueName());
        assertEquals("pingInboundQueue", details.getPingInboundQueueName());
        assertEquals("pingOutboundQueue", details.getPingOutboundQueueName());
        assertEquals("user", details.getUserName());
        assertEquals("password", details.getPassword());
        assertEquals("mock", details.getTypeOfQueue());
    }
}