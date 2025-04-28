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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ProxyAbisConfigServiceImplTest {

    @Mock
    private ProxyAbisInsertRepository proxyAbisInsertRepository;

    @Mock
    private ProxyAbisBioDataRepository proxyAbisBioDataRepository;

    @Mock
    private ExpectationCache expectationCache;

    @InjectMocks
    private ProxyAbisConfigServiceImpl proxyAbisConfigService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        proxyAbisConfigService.setDuplicate(true);
    }

    // Test getDuplicate()
    @Test
    void testGetDuplicate() {
        // When the property is set to true
        assertTrue(proxyAbisConfigService.getDuplicate());
    }

    // Test setDuplicate()
    @Test
    void testSetDuplicate() {
        proxyAbisConfigService.setDuplicate(false);
        assertFalse(proxyAbisConfigService.getDuplicate());
    }

    // Test isForceDuplicate()
    @Test
    void testIsForceDuplicate() {
        // Assuming the default value is false
        assertFalse(proxyAbisConfigService.isForceDuplicate());
    }

    // Test getExpectations()
    @Test
    void testGetExpectations() {
        Map<String, Expectation> mockExpectations = new HashMap<>();
        mockExpectations.put("exp1", new Expectation());

        when(expectationCache.get()).thenReturn(mockExpectations);

        Map<String, Expectation> result = proxyAbisConfigService.getExpectations();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test setExpectation()
    @Test
    void testSetExpectation() {
        Expectation exp = new Expectation();
        proxyAbisConfigService.setExpectation(exp);

        verify(expectationCache, times(1)).insert(exp);
    }

    // Test deleteExpectation()
    @Test
    void testDeleteExpectation() {
        String expId = "exp1";
        proxyAbisConfigService.deleteExpectation(expId);

        verify(expectationCache, times(1)).delete(expId);
    }

    // Test deleteExpectations()
    @Test
    void testDeleteExpectations() {
        proxyAbisConfigService.deleteExpectations();

        verify(expectationCache, times(1)).deleteAll();
    }

    // Test getCachedBiometrics()
    @Test
    void testGetCachedBiometrics() {
        List<String> mockBiometrics = List.of("bio1", "bio2");
        when(proxyAbisBioDataRepository.fetchAllBioData()).thenReturn(mockBiometrics);

        List<String> result = proxyAbisConfigService.getCachedBiometrics();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // Test getCachedBiometric()
    @Test
    void testGetCachedBiometric() {
        String hash = "someHash";
        List<String> mockBiometrics = List.of("bio1");
        when(proxyAbisBioDataRepository.fetchByBioData(hash)).thenReturn(mockBiometrics);

        List<String> result = proxyAbisConfigService.getCachedBiometric(hash);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test deleteAllCachedBiometrics()
    @Test
    void testDeleteAllCachedBiometrics() {
        proxyAbisConfigService.deleteAllCachedBiometrics();

        verify(proxyAbisBioDataRepository, times(1)).deleteAll();
        verify(proxyAbisInsertRepository, times(1)).deleteAll();
    }
}

