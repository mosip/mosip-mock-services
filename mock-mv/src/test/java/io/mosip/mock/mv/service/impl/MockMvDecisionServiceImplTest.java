package io.mosip.mock.mv.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.ExpectationCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for MockMvDecisionServiceImpl that validates decision service operations
 * and its interactions with the expectation cache.
 */
class MockMvDecisionServiceImplTest {

    private MockMvDecisionServiceImpl service;

    @Mock
    private ExpectationCache expectationCache;

    /**
     * Sets up the test environment before each test method.
     * Initializes Mockito annotations and creates a service instance with mocked cache.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MockMvDecisionServiceImpl(expectationCache);
    }

    /**
     * Tests the getter and setter methods for mock MV decision.
     * Verifies that:
     * 1. Initial decision value is set correctly
     * 2. Updated decision value is reflected properly
     */
    @Test
    void getAndSetMockMvDecision_ValidDecisions_ReturnsExpectedValues() {
        service.setMockMvDecision("DECISION_1");
        assertEquals("DECISION_1", service.getMockMvDecision());

        service.setMockMvDecision("DECISION_2");
        assertEquals("DECISION_2", service.getMockMvDecision());
    }

    /**
     * Tests retrieval of all expectations from the cache.
     * Verifies that the service correctly delegates to the cache
     * and returns the expected map of expectations.
     */
    @Test
    void getExpectations_ValidCache_ReturnsExpectationMap() {
        Map<String, Expectation> dummyMap = Collections.singletonMap("id1", new Expectation());
        when(expectationCache.get()).thenReturn(dummyMap);

        Map<String, Expectation> result = service.getExpectations();
        assertEquals(dummyMap, result);
        verify(expectationCache).get();
    }

    /**
     * Tests setting a new expectation.
     * Verifies that the service properly delegates the insert
     * operation to the cache.
     */
    @Test
    void setExpectation_ValidExpectation_InsertsIntoCache() {
        Expectation exp = new Expectation();
        service.setExpectation(exp);
        verify(expectationCache).insert(exp);
    }

    /**
     * Tests deletion of a specific expectation by ID.
     * Verifies that the service correctly delegates the delete
     * operation to the cache with the specified ID.
     */
    @Test
    void deleteExpectation_ValidId_DeletesFromCache() {
        String testId = "rid1";
        service.deleteExpectation(testId);
        verify(expectationCache).delete(testId);
    }

    /**
     * Tests deletion of all expectations.
     * Verifies that the service properly delegates the deleteAll
     * operation to the cache.
     */
    @Test
    void deleteExpectations_CallsDeleteAllOnCache() {
        service.deleteExpectations();
        verify(expectationCache).deleteAll();
    }

    /**
     * Tests retrieval of a specific expectation by ID.
     * Verifies that the service correctly delegates to the cache
     * and returns the expected expectation object.
     */
    @Test
    void getExpectation_ValidId_ReturnsExpectedValue() {
        Expectation exp = new Expectation();
        String testId = "rid2";
        when(expectationCache.get(testId)).thenReturn(exp);

        Expectation result = service.getExpectation(testId);
        assertEquals(exp, result);
        verify(expectationCache).get(testId);
    }
}