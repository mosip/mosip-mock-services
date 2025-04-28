package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dto.Expectation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpectationCacheImplTest {

    private ExpectationCacheImpl cache;

    @BeforeEach
    void setUp() {
        cache = new ExpectationCacheImpl();
    }

    @Test
    void testInsertAndGet() {
        Expectation ex = new Expectation();
        ex.setId("123");

        cache.insert(ex);
        Expectation result = cache.get("123");

        assertNotNull(result);
        assertEquals("123", result.getId());
    }

    @Test
    void testGetNonExistingReturnsEmptyObject() {
        Expectation result = cache.get("nonexistent");

        assertNotNull(result); // Should not return null
        assertNull(result.getId()); // Since it's a new Expectation
    }

    @Test
    void testDeleteExisting() {
        Expectation ex = new Expectation();
        ex.setId("abc");
        cache.insert(ex);

        boolean deleted = cache.delete("abc");

        assertTrue(deleted);
        assertNull(cache.get("abc").getId()); // new Expectation should be returned
    }

    @Test
    void testDeleteNonExisting() {
        boolean deleted = cache.delete("not-there");
        assertFalse(deleted);
    }

    @Test
    void testDeleteAll() {
        Expectation e1 = new Expectation();
        e1.setId("1");
        Expectation e2 = new Expectation();
        e2.setId("2");

        cache.insert(e1);
        cache.insert(e2);

        cache.deleteAll();

        assertEquals(0, cache.get().size());
    }

    @Test
    void testGetAll() {
        Expectation e1 = new Expectation();
        e1.setId("one");
        cache.insert(e1);

        Map<String, Expectation> all = cache.get();

        assertEquals(1, all.size());
        assertTrue(all.containsKey("one"));
    }
}

