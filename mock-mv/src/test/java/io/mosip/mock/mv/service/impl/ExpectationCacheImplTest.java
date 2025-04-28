package io.mosip.mock.mv.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpectationCacheImplTest {
    private ExpectationCacheImpl cache;

    @BeforeEach
    void setUp() {
        cache = new ExpectationCacheImpl();
    }

    @Test
    void testInsertAndGetAll() {
        Expectation expectation = new Expectation();
        // Assume that setRId method exists in Expectation
        expectation.setRId("test1");
        cache.insert(expectation);

        Map<String, Expectation> allExpectations = cache.get();
        assertEquals(1, allExpectations.size());
        assertTrue(allExpectations.containsKey("test1"));
        assertEquals(expectation, allExpectations.get("test1"));
    }

    @Test
    void testGetByIdExisting() {
        Expectation expectation = new Expectation();
        expectation.setRId("test2");
        cache.insert(expectation);

        Expectation retrieved = cache.get("test2");
        assertEquals(expectation, retrieved);
    }

    @Test
    void testGetByIdNotExisting() {
        // When a non-existent key is requested, a new Expectation is returned.
        Expectation retrieved = cache.get("nonexistent");
        assertNotNull(retrieved);
    }

    @Test
    void testDelete() {
        Expectation expectation = new Expectation();
        expectation.setRId("test3");
        cache.insert(expectation);
        // Delete the expectation by its RId.
        cache.delete("test3");

        Map<String, Expectation> allExpectations = cache.get();
        assertFalse(allExpectations.containsKey("test3"));
    }

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