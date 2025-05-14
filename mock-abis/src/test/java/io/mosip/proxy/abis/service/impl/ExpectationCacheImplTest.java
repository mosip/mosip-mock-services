package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dto.Expectation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test class for ExpectationCacheImpl.
 * This class tests the functionality of the ExpectationCacheImpl class, including
 * insertion, retrieval, deletion, and clearing of expectations.
 */
class ExpectationCacheImplTest {

    private ExpectationCacheImpl cache;

    /**
     * Sets up the test environment before each test.
     * Initializes a new instance of ExpectationCacheImpl.
     */
    @BeforeEach
    void setUp() {
        cache = new ExpectationCacheImpl();
    }

    /**
     * Tests the insert and get methods.
     * Verifies that an inserted expectation can be retrieved correctly.
     */
    @Test
    void testInsertAndGet() {
        // Given
        Expectation ex = new Expectation();
        ex.setId("123");

        // When
        cache.insert(ex);
        Expectation result = cache.get("123");

        // Then
        assertNotNull(result); // Verify the result is not null
        assertEquals("123", result.getId()); // Verify the ID matches
    }

    /**
     * Tests the get method for a non-existing key.
     * Verifies that it returns a new empty Expectation object.
     */
    @Test
    void testGetNonExistingReturnsEmptyObject() {
        // When
        Expectation result = cache.get("nonexistent");

        // Then
        assertNotNull(result); // Verify the result is not null
        assertNull(result.getId()); // Verify the ID is null
    }

    /**
     * Tests the delete method for an existing key.
     * Verifies that the key is deleted and a new empty Expectation is returned.
     */
    @Test
    void testDeleteExisting() {
        // Given
        Expectation ex = new Expectation();
        ex.setId("abc");
        cache.insert(ex);

        // When
        boolean deleted = cache.delete("abc");

        // Then
        assertTrue(deleted); // Verify the deletion was successful
        assertNull(cache.get("abc").getId()); // Verify a new Expectation is returned
    }

    /**
     * Tests the delete method for a non-existing key.
     * Verifies that it returns false.
     */
    @Test
    void testDeleteNonExisting() {
        // When
        boolean deleted = cache.delete("not-there");

        // Then
        assertFalse(deleted); // Verify the deletion was unsuccessful
    }

    /**
     * Tests the deleteAll method.
     * Verifies that all expectations are cleared from the cache.
     */
    @Test
    void testDeleteAll() {
        // Given
        Expectation e1 = new Expectation();
        e1.setId("1");
        Expectation e2 = new Expectation();
        e2.setId("2");

        cache.insert(e1);
        cache.insert(e2);

        // When
        cache.deleteAll();

        // Then
        assertEquals(0, cache.get().size()); // Verify the cache is empty
    }

    /**
     * Tests the get method for retrieving all expectations.
     * Verifies that all inserted expectations are returned.
     */
    @Test
    void testGetAll() {
        // Given
        Expectation e1 = new Expectation();
        e1.setId("one");
        cache.insert(e1);

        // When
        Map<String, Expectation> all = cache.get();

        // Then
        assertEquals(1, all.size()); // Verify the size of the map
        assertTrue(all.containsKey("one")); // Verify the key exists
    }
}