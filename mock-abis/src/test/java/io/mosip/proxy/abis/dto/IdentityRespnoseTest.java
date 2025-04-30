package io.mosip.proxy.abis.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for IdentityResponse.
 * This class tests the functionality of the IdentityResponse class, including its constructors,
 * getters, setters, and nested classes.
 */
class IdentityResponseTest {

    /**
     * Tests the parameterized constructor of IdentityResponse.
     * Verifies that all fields are correctly initialized.
     */
    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        IdentityResponse.Analytics analytics = new IdentityResponse.Analytics("0.9", "85", "key1Value", "key2Value");
        IdentityResponse.CandidateList candidateList = new IdentityResponse.CandidateList("1", new ArrayList<>());

        IdentityResponse response = new IdentityResponse("id1", "req123", now, "Success", candidateList, analytics);

        assertEquals("id1", response.getId());
        assertEquals("req123", response.getRequestId());
        assertEquals(now, response.getResponsetime());
        assertEquals("Success", response.getReturnValue());
        assertEquals(candidateList, response.getCandidateList());
        assertEquals(analytics, response.getAnalytics());
    }

    /**
     * Tests the default constructor of IdentityResponse.
     * Verifies that all fields are initialized to their default values.
     */
    @Test
    void testDefaultConstructor() {
        IdentityResponse response = new IdentityResponse();

        assertNull(response.getId());
        assertNull(response.getRequestId());
        assertNull(response.getResponsetime());
        assertNull(response.getReturnValue());
        assertNull(response.getCandidateList());
        assertNotNull(response.getAnalytics()); // Default analytics object
    }

    /**
     * Tests the CandidateList nested class.
     * Verifies that candidates can be added and retrieved correctly.
     */
    @Test
    void testCandidateList() {
        IdentityResponse.CandidateList candidateList = new IdentityResponse.CandidateList();
        IdentityResponse.Candidates candidate1 = new IdentityResponse.Candidates("ref1", null, null);
        IdentityResponse.Candidates candidate2 = new IdentityResponse.Candidates("ref2", null, null);

        List<IdentityResponse.Candidates> candidates = new ArrayList<>();
        candidates.add(candidate1);
        candidates.add(candidate2);

        candidateList.setCandidates(candidates);
        candidateList.setCount(String.valueOf(candidates.size()));

        assertEquals("2", candidateList.getCount());
        assertEquals(2, candidateList.getCandidates().size());
        assertEquals("ref1", candidateList.getCandidates().get(0).getReferenceId());
        assertEquals("ref2", candidateList.getCandidates().get(1).getReferenceId());
    }

    /**
     * Tests the Modalities nested class.
     * Verifies that biometric type and analytics are correctly set and retrieved.
     */
    @Test
    void testModalities() {
        IdentityResponse.Analytics analytics = new IdentityResponse.Analytics("0.8", "75", "key1", "key2");
        IdentityResponse.Modalities modalities = new IdentityResponse.Modalities("Fingerprint", analytics);

        assertEquals("Fingerprint", modalities.getBiometricType());
        assertEquals(analytics, modalities.getAnalytics());
    }

    /**
     * Tests the Analytics nested class.
     * Verifies that all fields are correctly set and retrieved.
     */
    @Test
    void testAnalytics() {
        IdentityResponse.Analytics analytics = new IdentityResponse.Analytics("0.95", "90", "key1Value", "key2Value");

        assertEquals("0.95", analytics.getConfidence());
        assertEquals("90", analytics.getInternalScore());
        assertEquals("key1Value", analytics.getKey1());
        assertEquals("key2Value", analytics.getKey2());

        analytics.setConfidence("0.85");
        analytics.setInternalScore("80");
        analytics.setKey1("newKey1");
        analytics.setKey2("newKey2");

        assertEquals("0.85", analytics.getConfidence());
        assertEquals("80", analytics.getInternalScore());
        assertEquals("newKey1", analytics.getKey1());
        assertEquals("newKey2", analytics.getKey2());
    }

    /**
     * Tests the Candidates nested class.
     * Verifies that reference ID, analytics, and modalities are correctly set and retrieved.
     */
    @Test
    void testCandidates() {
        IdentityResponse.Analytics analytics = new IdentityResponse.Analytics("0.7", "65", "key1", "key2");
        IdentityResponse.Modalities modality = new IdentityResponse.Modalities("Iris", analytics);
        List<IdentityResponse.Modalities> modalities = new ArrayList<>();
        modalities.add(modality);

        IdentityResponse.Candidates candidate = new IdentityResponse.Candidates("ref123", analytics, modalities);

        assertEquals("ref123", candidate.getReferenceId());
        assertEquals(analytics, candidate.getAnalytics());
        assertEquals(1, candidate.getModalities().size());
        assertEquals("Iris", candidate.getModalities().get(0).getBiometricType());
    }
}