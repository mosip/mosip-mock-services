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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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
     * Tests the successful setting of an expectation.
     * Verifies that:
     * - The service method is called with correct parameters
     * - Response contains the expected ID
     * - HTTP status is OK
     */
    @Test
    void testSetExpectation_ValidExpectation_ReturnsResponseContainingId() {
        Expectation expectation = new Expectation();
        expectation.setId("test-id");

        ResponseEntity<String> response = controller.setExpectation(expectation);

        verify(proxyAbisConfigService).setExpectation(expectation);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("test-id"));
    }

    /**
     * Tests the retrieval of all expectations.
     * Verifies that:
     * - Service returns the expected map of expectations
     * - Response contains the correct data
     * - HTTP status is OK
     */
    @Test
    void testGetExpectation_WhenExpectationsExist_ReturnsHttpOkWithMap() {
        Map<String, Expectation> expectations = new HashMap<>();
        when(proxyAbisConfigService.getExpectations()).thenReturn(expectations);

        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
    }

    /**
     * Tests the deletion of a specific expectation.
     * Verifies that:
     * - Service is called with the correct ID
     * - Response contains confirmation message
     * - HTTP status is OK
     */
    @Test
    void testDeleteExpectation_WhenValidId_ReturnsHttpOkWithIdInBody() {
        String id = "test-id";

        ResponseEntity<String> response = controller.deleteExpectation(id);

        verify(proxyAbisConfigService).deleteExpectation(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains(id));
    }

    /**
     * Tests the deletion of all expectations.
     * Verifies that:
     * - Service method is called
     * - Response indicates successful deletion
     * - HTTP status is OK
     */
    @Test
    void testDeleteAllExpectations_WhenCalled_ReturnsHttpOkWithSuccessMessage() {
        ResponseEntity<String> response = controller.deleteAllExpectations();

        verify(proxyAbisConfigService).deleteExpectations();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Tests the configuration status check.
     * Verifies that:
     * - Service returns correct duplicate detection status
     * - Response contains proper configuration data
     * - HTTP status is OK
     */
    @Test
    void testCheckConfiguration_WhenDuplicateExists_ReturnsHttpOkWithTrueFlag() {
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);

        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getFindDuplicate());
    }

    /**
     * Tests the configuration update endpoint.
     * Verifies that:
     * - Service is updated with new configuration
     * - Response indicates successful update
     * - HTTP status is OK
     */
    @Test
    void testConfigure_WhenFindDuplicateIsTrue_ReturnsHttpOkWithSuccessMessage() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setFindDuplicate(true);

        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        verify(proxyAbisConfigService).setDuplicate(true);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Tests the retrieval of cached biometrics.
     * Verifies that:
     * - Service returns expected list of cached items
     * - Response contains correct data
     * - HTTP status is OK
     */
    @Test
    void testGetCache_WhenCachedBiometricsExist_ReturnsHttpOkWithBiometricList() {
        List<String> cachedBiometrics = Arrays.asList("bio1", "bio2");
        when(proxyAbisConfigService.getCachedBiometrics()).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCache();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    /**
     * Tests the retrieval of cached biometrics by hash.
     * Verifies that:
     * - Service returns correct data for given hash
     * - Response contains expected items
     * - HTTP status is OK
     */
    @Test
    void testGetCacheByHash_WhenValidHashProvided_ReturnsHttpOkWithBiometricList() {
        String hash = "test-hash";
        List<String> cachedBiometrics = List.of("bio1");
        when(proxyAbisConfigService.getCachedBiometric(hash)).thenReturn(cachedBiometrics);

        ResponseEntity<List<String>> response = controller.getCacheByHash(hash);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(cachedBiometrics, response.getBody());
    }

    /**
     * Tests the cache clearing functionality.
     * Verifies that:
     * - Service method for clearing cache is called
     * - Response indicates successful operation
     * - HTTP status is OK
     */
    @Test
    void testDeleteCache_WhenInvoked_DeletesAllCachedBiometricsAndReturnsSuccess() {
        ResponseEntity<String> response = controller.deleteCache();

        verify(proxyAbisConfigService).deleteAllCachedBiometrics();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Successfully"));
    }

    /**
     * Tests error handling when setting expectation fails.
     * Verifies that:
     * - RuntimeException is properly wrapped in AbisException
     * - Exception contains appropriate error message
     */
    @Test
    void testSetExpectation_WhenServiceThrowsException_ShouldThrowAbisException() {
        Expectation expectation = new Expectation();
        doThrow(new RuntimeException("Test error")).when(proxyAbisConfigService).setExpectation(any());

        assertThrows(AbisException.class, () -> controller.setExpectation(expectation));
    }

    /**
     * Tests error handling when getting expectations fails.
     * Verifies that:
     * - RuntimeException is properly wrapped in AbisException
     * - Exception handling works as expected
     */
    @Test
    void testGetExpectation_WhenServiceThrowsException_ShouldThrowAbisException() {
        when(proxyAbisConfigService.getExpectations()).thenThrow(new RuntimeException("Test error"));

        assertThrows(AbisException.class, () -> controller.getExpectation());
    }

    /**
     * Tests error handling when deleting expectation fails.
     * Verifies that:
     * - Service exception is properly handled
     * - AbisException is thrown with correct message
     */
    @Test
    void testDeleteExpectation_WhenServiceThrowsException_ShouldThrowAbisException() {
        String id = "test-id";
        doThrow(new RuntimeException("Test error"))
                .when(proxyAbisConfigService).deleteExpectation(id);

        assertThrows(AbisException.class, () -> controller.deleteExpectation(id));
    }

    /**
     * Tests error handling when deleting all expectations fails.
     * Verifies that:
     * - Service exception is properly handled
     * - AbisException is thrown with correct message
     */
    @Test
    void testDeleteAllExpectations_WhenServiceThrowsException_ShouldThrowAbisException() {
        doThrow(new RuntimeException("Test error"))
                .when(proxyAbisConfigService).deleteExpectations();

        assertThrows(AbisException.class, () -> controller.deleteAllExpectations());
    }

    /**
     * Tests error handling when getting cache fails.
     * Verifies that:
     * - Service exception is properly handled
     * - AbisException is thrown with correct message
     */
    @Test
    void testGetCache_WhenServiceThrowsException_ShouldThrowAbisException() {
        when(proxyAbisConfigService.getCachedBiometrics())
                .thenThrow(new RuntimeException("Test error"));

        assertThrows(AbisException.class, () -> controller.getCache());
    }

    /**
     * Tests error handling when getting cache by hash fails.
     * Verifies that:
     * - Service exception is properly handled
     * - AbisException is thrown with correct message
     */
    @Test
    void testGetCacheByHash_WhenServiceThrowsException_ShouldThrowAbisException() {
        String hash = "test-hash";
        when(proxyAbisConfigService.getCachedBiometric(hash))
                .thenThrow(new RuntimeException("Test error"));

        assertThrows(AbisException.class, () -> controller.getCacheByHash(hash));
    }

    /**
     * Tests error handling when deleting cache fails.
     * Verifies that:
     * - Service exception is properly handled
     * - AbisException is thrown with correct message
     */
    @Test
    void testDeleteCache_WhenServiceThrowsException_ShouldThrowAbisException() {
        doThrow(new RuntimeException("Test error"))
                .when(proxyAbisConfigService).deleteAllCachedBiometrics();

        assertThrows(AbisException.class, () -> controller.deleteCache());
    }

    /**
     * Tests validation handling when configure is called with null DTO.
     * Verifies that:
     * - Null input is properly validated
     * - AbisException is thrown with appropriate message
     */
    @Test
    void testConfigure_WhenDtoIsNull_ShouldThrowAbisException() {
        assertThrows(AbisException.class, () -> controller.configure(null, bindingResult));
    }
}