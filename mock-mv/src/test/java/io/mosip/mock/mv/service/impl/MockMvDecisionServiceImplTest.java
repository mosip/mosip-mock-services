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

class MockMvDecisionServiceImplTest {

    private MockMvDecisionServiceImpl service;

    @Mock
    private ExpectationCache expectationCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // initialize service with mocked expectationCache
        service = new MockMvDecisionServiceImpl(expectationCache);
    }

    @Test
    void testGetAndSetMockMvDecision() {
        // Initially set a default decision via setter.
        service.setMockMvDecision("DECISION_1");
        assertEquals("DECISION_1", service.getMockMvDecision());

        // Update decision value and test again.
        service.setMockMvDecision("DECISION_2");
        assertEquals("DECISION_2", service.getMockMvDecision());
    }

    @Test
    void testGetExpectations() {
        Map<String, Expectation> dummyMap = Collections.singletonMap("id1", new Expectation());
        when(expectationCache.get()).thenReturn(dummyMap);

        Map<String, Expectation> result = service.getExpectations();
        assertEquals(dummyMap, result);
        verify(expectationCache).get();
    }

    @Test
    void testSetExpectation() {
        Expectation exp = new Expectation();

        // Call method
        service.setExpectation(exp);
        // Verify that insert is called.
        verify(expectationCache).insert(exp);
    }

    @Test
    void testDeleteExpectation() {
        String testId = "rid1";

        // Call method
        service.deleteExpectation(testId);
        // Verify that delete is called.
        verify(expectationCache).delete(testId);
    }

    @Test
    void testDeleteExpectations() {
        // Call method
        service.deleteExpectations();
        // Verify that deleteAll is called.
        verify(expectationCache).deleteAll();
    }

    @Test
    void testGetExpectation() {
        Expectation exp = new Expectation();
        String testId = "rid2";
        when(expectationCache.get(testId)).thenReturn(exp);

        Expectation result = service.getExpectation(testId);
        assertEquals(exp, result);
        verify(expectationCache).get(testId);
    }
}