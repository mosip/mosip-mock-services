package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for IdentityRequest.
 * This class tests the functionality of the IdentityRequest class, including its constructors,
 * getters, setters, and nested classes.
 */
class IdentityRequestTest {

    /**
     * Tests the parameterized constructor of IdentityRequest.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        IdentityRequest.Gallery gallery = new IdentityRequest.Gallery();
        IdentityRequest.Flags flags = new IdentityRequest.Flags("10", "0.01", "flag1Value", "flag2Value");

        IdentityRequest request = new IdentityRequest("id1", "1.0", "req123", now, "refId1", "http://example.com", gallery, flags);

        assertEquals("id1", request.getId());
        assertEquals("1.0", request.getVersion());
        assertEquals("req123", request.getRequestId());
        assertEquals(now, request.getRequesttime());
        assertEquals("refId1", request.getReferenceId());
        assertEquals("http://example.com", request.getReferenceUrl());
        assertEquals(gallery, request.getGallery());
        assertEquals(flags, request.getFlags());
    }

    /**
     * Tests the default constructor of IdentityRequest.
     * Verifies that all fields are initialized to their default values.
     */
    @Test
    void testDefaultConstructor() {
        IdentityRequest request = new IdentityRequest();

        assertNull(request.getId());
        assertNull(request.getVersion());
        assertNull(request.getRequestId());
        assertNull(request.getRequesttime());
        assertNull(request.getReferenceId());
        assertNull(request.getReferenceUrl());
        assertNull(request.getGallery());
        assertNull(request.getFlags());
    }

    /**
     * Tests the Gallery nested class.
     * Verifies that reference IDs can be added and retrieved correctly.
     */
    @Test
    void testGallery() {
        IdentityRequest.Gallery gallery = new IdentityRequest.Gallery();
        IdentityRequest.ReferenceIds refId1 = new IdentityRequest.ReferenceIds("ref1");
        IdentityRequest.ReferenceIds refId2 = new IdentityRequest.ReferenceIds("ref2");

        List<IdentityRequest.ReferenceIds> referenceIds = new ArrayList<>();
        referenceIds.add(refId1);
        referenceIds.add(refId2);

        gallery.setReferenceIds(referenceIds);

        assertEquals(2, gallery.getReferenceIds().size());
        assertEquals("ref1", gallery.getReferenceIds().get(0).getReferenceId());
        assertEquals("ref2", gallery.getReferenceIds().get(1).getReferenceId());
    }

    /**
     * Tests the ReferenceIds nested class.
     * Verifies that the reference ID is correctly set and retrieved.
     */
    @Test
    void testReferenceIds() {
        IdentityRequest.ReferenceIds referenceIds = new IdentityRequest.ReferenceIds("ref123");

        assertEquals("ref123", referenceIds.getReferenceId());

        referenceIds.setReferenceId("ref456");
        assertEquals("ref456", referenceIds.getReferenceId());
    }

    /**
     * Tests the Flags nested class.
     * Verifies that all fields are correctly set and retrieved.
     */
    @Test
    void testFlags() {
        IdentityRequest.Flags flags = new IdentityRequest.Flags("10", "0.01", "flag1Value", "flag2Value");

        assertEquals("10", flags.getMaxResults());
        assertEquals("0.01", flags.getTargetFPIR());
        assertEquals("flag1Value", flags.getFlag1());
        assertEquals("flag2Value", flags.getFlag2());

        flags.setMaxResults("20");
        flags.setTargetFPIR("0.02");
        flags.setFlag1("newFlag1");
        flags.setFlag2("newFlag2");

        assertEquals("20", flags.getMaxResults());
        assertEquals("0.02", flags.getTargetFPIR());
        assertEquals("newFlag1", flags.getFlag1());
        assertEquals("newFlag2", flags.getFlag2());
    }
}