package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for the Expectation class.
 * This class tests the functionality of the Expectation class, including its constructors,
 * getters, setters, and nested classes.
 */
class ExpectationTest {

    /**
     * Tests the parameterized constructor of the Expectation class.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Expectation.Gallery gallery = new Expectation.Gallery();
        Expectation expectation = new Expectation("id1", "1.0", now, "Insert", "Success", gallery);

        assertEquals("id1", expectation.getId());
        assertEquals("1.0", expectation.getVersion());
        assertEquals(now, expectation.getRequesttime());
        assertEquals("Insert", expectation.getActionToInterfere());
        assertEquals("Success", expectation.getForcedResponse());
        assertEquals(gallery, expectation.getGallery());
    }

    /**
     * Tests the default constructor of the Expectation class.
     * Verifies that all fields are initialized to their default values.
     */
    @Test
    void testDefaultConstructor() {
        Expectation expectation = new Expectation();

        assertNull(expectation.getId());
        assertNull(expectation.getVersion());
        assertNull(expectation.getRequesttime());
        assertNull(expectation.getActionToInterfere());
        assertNull(expectation.getForcedResponse());
        assertNull(expectation.getGallery());
    }

    /**
     * Tests the Gallery nested class.
     * Verifies that reference IDs can be added and retrieved correctly.
     */
    @Test
    void testGallery() {
        Expectation.Gallery gallery = new Expectation.Gallery();
        Expectation.ReferenceIds refId1 = new Expectation.ReferenceIds("ref1");
        Expectation.ReferenceIds refId2 = new Expectation.ReferenceIds("ref2");

        List<Expectation.ReferenceIds> referenceIds = new ArrayList<>();
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
        Expectation.ReferenceIds referenceIds = new Expectation.ReferenceIds("ref123");

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
        Expectation.Flags flags = new Expectation.Flags("10", "0.01", "flag1Value", "flag2Value");

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