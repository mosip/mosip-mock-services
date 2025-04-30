package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.dto.ConfigureDto;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for ProxyAbisConfigController.
 * This class verifies the behavior of the controller methods
 * and their interactions with the service layer.
 */
@ExtendWith(MockitoExtension.class)
class ProxyAbisConfigControllerTest {

    @Mock
    private ProxyAbisConfigService proxyAbisConfigService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ProxyAbisConfigController controller;

    /**
     * Test to verify the setExpectation method.
     * Ensures the service is called and the response contains the expected data.
     */
    @Test
    void testSetExpectation() {
        Expectation expectation = new Expectation();
        expectation.setId("test-id");

        ResponseEntity<String> response = controller.setExpectation(expectation);

        verify(proxyAbisConfigService).setExpectation(expectation);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("test-id"));
    }

    /**
     * Test to verify the getExpectation method.
     * Ensures the service returns the expected map of expectations.
     */
    @Test
    void testGetExpectation() {
        Map<String, Expectation> expectations = new HashMap<>();
        when(proxyAbisConfigService.getExpectations()).thenReturn(expectations);

        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
    }

    /**
     * Test to verify the deleteExpectation method.
     * Ensures the service is called with the correct ID and the response is as expected.
     */
    @Test
    void testDeleteExpectation() {
        String id = "test-id";

        ResponseEntity<String> response = controller.deleteExpectation(id);

        verify(proxyAbisConfigService).deleteExpectation(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains(id));
    }

    /**
     * Test to verify the deleteAllExpectations method.
     * Ensures the service is called and the response indicates success.
     */
    @Test
    void testDeleteAllExpectations() {
        ResponseEntity<String> response = controller.deleteAllExpectations();

        verify(proxyAbisConfigService).deleteExpectations();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Test to verify the checkConfiguration method.
     * Ensures the service returns the correct configuration status.
     */
    @Test
    void testCheckConfiguration() {
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);

        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getFindDuplicate());
    }

    /**
     * Test to verify the configure method.
     * Ensures the service is called with the correct configuration and the response indicates success.
     */
    @Test
    void testConfigure() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setFindDuplicate(true);

        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        verify(proxyAbisConfigService).setDuplicate(true);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Test to verify the getCache method.
     * Ensures the service returns the expected list of cached biometrics.
     */
    @Test
    void testGetCache() {
        List<String> cachedBiometrics = Arrays.asList("bio1", "bio2");
        when(proxyAbisConfigService.getCachedBiometrics()).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCache();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    /**
     * Test to verify the getCacheByHash method.
     * Ensures the service returns the expected cached biometrics for a given hash.
     */
    @Test
    void testGetCacheByHash() {
        String hash = "test-hash";
        List<String> cachedBiometrics = List.of("bio1");
        when(proxyAbisConfigService.getCachedBiometric(hash)).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCacheByHash(hash);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    /**
     * Test to verify the deleteCache method.
     * Ensures the service is called and the response indicates success.
     */
    @Test
    void testDeleteCache() {
        ResponseEntity<String> response = controller.deleteCache();

        verify(proxyAbisConfigService).deleteAllCachedBiometrics();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Test to verify the setExpectation method when an exception is thrown.
     * Ensures the exception is correctly handled and wrapped in an AbisException.
     */
    @Test
    void testSetExpectationWithException() {
        Expectation expectation = new Expectation();
        doThrow(new RuntimeException("Test error")).when(proxyAbisConfigService).setExpectation(any());

        assertThrows(AbisException.class, () -> controller.setExpectation(expectation));
    }

    /**
     * Test to verify the getExpectation method when an exception is thrown.
     * Ensures the exception is correctly handled and wrapped in an AbisException.
     */
    @Test
    void testGetExpectationWithException() {
        when(proxyAbisConfigService.getExpectations()).thenThrow(new RuntimeException("Test error"));

        assertThrows(AbisException.class, () -> controller.getExpectation());
    }
}