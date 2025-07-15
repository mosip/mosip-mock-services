package io.mosip.proxy.abis.service.impl;

import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.service.ExpectationCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit test class for ProxyAbisConfigServiceImpl.
 * This class tests the functionality of ProxyAbisConfigServiceImpl, including
 * managing expectations, biometrics, and duplicate configurations.
 */
class ProxyAbisConfigServiceImplTest {

    @Mock
    private ProxyAbisInsertRepository proxyAbisInsertRepository;

    @Mock
    private ProxyAbisBioDataRepository proxyAbisBioDataRepository;

    @Mock
    private ExpectationCache expectationCache;

    @InjectMocks
    private ProxyAbisConfigServiceImpl proxyAbisConfigService;

    /**
     * Sets up the test environment before each test.
     * Initializes mocks and injects them into the ProxyAbisConfigServiceImpl instance.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        proxyAbisConfigService.setDuplicate(true); // Set initial duplicate value
    }

    /**
     * Tests the getDuplicate method.
     * Verifies that the duplicate property is returned correctly.
     */
    @Test
    void getDuplicate_returnsTrue_whenDuplicateIsSet() {
        assertTrue(proxyAbisConfigService.getDuplicate()); // Verify the duplicate property is true
    }

    /**
     * Tests the setDuplicate method.
     * Verifies that the duplicate property is updated correctly.
     */
    @Test
    void setDuplicate_updatesDuplicatePropertyToFalse() {
        proxyAbisConfigService.setDuplicate(false);
        assertFalse(proxyAbisConfigService.getDuplicate()); // Verify the duplicate property is false
    }

    /**
     * Tests the isForceDuplicate method.
     * Verifies that the default value of forceDuplicate is false.
     */
    @Test
    void isForceDuplicate_returnsFalseByDefault() {
        assertFalse(proxyAbisConfigService.isForceDuplicate()); // Verify the default value is false
    }

    /**
     * Tests the getExpectations method.
     * Verifies that expectations are retrieved correctly from the cache.
     */
    @Test
    void getExpectations_returnsNonNullMapWithExpectedSize() {
        Map<String, Expectation> mockExpectations = new HashMap<>();
        mockExpectations.put("exp1", new Expectation());

        when(expectationCache.get()).thenReturn(mockExpectations);

        Map<String, Expectation> result = proxyAbisConfigService.getExpectations();
        assertNotNull(result); // Verify the result is not null
        assertEquals(1, result.size()); // Verify the size of the result
    }

    /**
     * Tests the setExpectation method.
     * Verifies that an expectation is inserted into the cache.
     */
    @Test
    void setExpectation_insertsExpectationIntoCache_successfully() {
        Expectation exp = new Expectation();
        proxyAbisConfigService.setExpectation(exp);

        verify(expectationCache, times(1)).insert(exp); // Verify the insert method is called once
    }

    /**
     * Tests the deleteExpectation method.
     * Verifies that an expectation is deleted from the cache.
     */
    @Test
    void deleteExpectation_deletesExpectationFromCache_successfully() {
        String expId = "exp1";
        proxyAbisConfigService.deleteExpectation(expId);

        verify(expectationCache, times(1)).delete(expId); // Verify the delete method is called once
    }

    /**
     * Tests the deleteExpectations method.
     * Verifies that all expectations are deleted from the cache.
     */
    @Test
    void deleteExpectations_deletesAllExpectationsFromCache_successfully() {
        proxyAbisConfigService.deleteExpectations();

        verify(expectationCache, times(1)).deleteAll(); // Verify the deleteAll method is called once
    }

    /**
     * Tests the getCachedBiometrics method.
     * Verifies that all cached biometrics are retrieved correctly.
     */
    @Test
    void getCachedBiometrics_returnsAllCachedBiometrics_successfully() {
        List<String> mockBiometrics = List.of("bio1", "bio2");
        when(proxyAbisBioDataRepository.fetchAllBioData()).thenReturn(mockBiometrics);

        List<String> result = proxyAbisConfigService.getCachedBiometrics();
        assertNotNull(result); // Verify the result is not null
        assertEquals(2, result.size()); // Verify the size of the result
    }

    /**
     * Tests the getCachedBiometric method.
     * Verifies that a specific cached biometric is retrieved correctly.
     */
    @Test
    void getCachedBiometric_returnsBiometricListForGivenHash_successfully() {
        String hash = "someHash";
        List<String> mockBiometrics = List.of("bio1");
        when(proxyAbisBioDataRepository.fetchByBioData(hash)).thenReturn(mockBiometrics);

        List<String> result = proxyAbisConfigService.getCachedBiometric(hash);
        assertNotNull(result); // Verify the result is not null
        assertEquals(1, result.size()); // Verify the size of the result
    }

    /**
     * Tests the deleteAllCachedBiometrics method.
     * Verifies that all cached biometrics are deleted from the repositories.
     */
    @Test
    void deleteAllCachedBiometrics_deletesAllBiometricsFromBothRepositories_successfully() {
        proxyAbisConfigService.deleteAllCachedBiometrics();

        verify(proxyAbisBioDataRepository, times(1)).deleteAll(); // Verify the deleteAll method is called on bioDataRepository
        verify(proxyAbisInsertRepository, times(1)).deleteAll(); // Verify the deleteAll method is called on insertRepository
    }
}