package io.mosip.mock.mv.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for ExpectationCacheImpl that validates the caching functionality
 * for Expectation objects.
 */
class ExpectationCacheImplTest {
    private ExpectationCacheImpl cache;

    /**
     * Initializes a fresh cache instance before each test method execution
     * to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        cache = new ExpectationCacheImpl();
    }

    /**
     * Tests the insertion of an expectation and retrieval of all cached expectations.
     * Verifies that:
     * 1. The inserted expectation is stored correctly
     * 2. The cache size is updated
     * 3. The expectation can be retrieved using its key
     */
    @Test
    void testInsertAndGetAll() {
        Expectation expectation = new Expectation();
        expectation.setRId("test1");
        cache.insert(expectation);

        Map<String, Expectation> allExpectations = cache.get();
        assertEquals(1, allExpectations.size());
        assertTrue(allExpectations.containsKey("test1"));
        assertEquals(expectation, allExpectations.get("test1"));
    }

    /**
     * Tests retrieval of an existing expectation by its ID.
     * Verifies that the retrieved expectation matches the one that was inserted.
     */
    @Test
    void testGetByIdExisting() {
        Expectation expectation = new Expectation();
        expectation.setRId("test2");
        cache.insert(expectation);

        Expectation retrieved = cache.get("test2");
        assertEquals(expectation, retrieved);
    }

    /**
     * Tests retrieval behavior when requesting a non-existent expectation ID.
     * Verifies that a new Expectation instance is returned instead of null.
     */
    @Test
    void testGetByIdNotExisting() {
        Expectation retrieved = cache.get("nonexistent");
        assertNotNull(retrieved);
    }

    /**
     * Tests the deletion of a specific expectation from the cache.
     * Verifies that the expectation is properly removed and no longer accessible.
     */
    @Test
    void testDelete() {
        Expectation expectation = new Expectation();
        expectation.setRId("test3");
        cache.insert(expectation);
        cache.delete("test3");

        Map<String, Expectation> allExpectations = cache.get();
        assertFalse(allExpectations.containsKey("test3"));
    }

    /**
     * Tests the deletion of all expectations from the cache.
     * Verifies that the cache is completely emptied after the operation.
     */
    @Test
    void testDeleteAll() {
        Expectation e1 = new Expectation();
        Expectation e2 = new Expectation();
        e1.setRId("id1");
        e2.setRId("id2");
        cache.insert(e1);
        cache.insert(e2);

        cache.deleteAll();
        Map<String, Expectation> allExpectations = cache.get();
        assertTrue(allExpectations.isEmpty());
    }
}