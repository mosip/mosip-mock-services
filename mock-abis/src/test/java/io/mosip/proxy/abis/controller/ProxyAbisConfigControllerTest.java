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

@ExtendWith(MockitoExtension.class)
class ProxyAbisConfigControllerTest {

    @Mock
    private ProxyAbisConfigService proxyAbisConfigService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ProxyAbisConfigController controller;

    @Test
    void testSetExpectation() {
        Expectation expectation = new Expectation();
        expectation.setId("test-id");

        ResponseEntity<String> response = controller.setExpectation(expectation);

        verify(proxyAbisConfigService).setExpectation(expectation);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("test-id"));
    }

    @Test
    void testGetExpectation() {
        Map<String, Expectation> expectations = new HashMap<>();
        when(proxyAbisConfigService.getExpectations()).thenReturn(expectations);

        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
    }

    @Test
    void testDeleteExpectation() {
        String id = "test-id";

        ResponseEntity<String> response = controller.deleteExpectation(id);

        verify(proxyAbisConfigService).deleteExpectation(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains(id));
    }

    @Test
    void testDeleteAllExpectations() {
        ResponseEntity<String> response = controller.deleteAllExpectations();

        verify(proxyAbisConfigService).deleteExpectations();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    @Test
    void testCheckConfiguration() {
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);

        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getFindDuplicate());
    }

    @Test
    void testConfigure() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setFindDuplicate(true);

        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        verify(proxyAbisConfigService).setDuplicate(true);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    @Test
    void testGetCache() {
        List<String> cachedBiometrics = Arrays.asList("bio1", "bio2");
        when(proxyAbisConfigService.getCachedBiometrics()).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCache();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    @Test
    void testGetCacheByHash() {
        String hash = "test-hash";
        List<String> cachedBiometrics = List.of("bio1");
        when(proxyAbisConfigService.getCachedBiometric(hash)).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCacheByHash(hash);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    @Test
    void testDeleteCache() {
        ResponseEntity<String> response = controller.deleteCache();

        verify(proxyAbisConfigService).deleteAllCachedBiometrics();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    @Test
    void testSetExpectationWithException() {
        Expectation expectation = new Expectation();
        doThrow(new RuntimeException("Test error")).when(proxyAbisConfigService).setExpectation(any());

        assertThrows(AbisException.class, () -> controller.setExpectation(expectation));
    }

    @Test
    void testGetExpectationWithException() {
        when(proxyAbisConfigService.getExpectations()).thenThrow(new RuntimeException("Test error"));

        assertThrows(AbisException.class, () -> controller.getExpectation());
    }
}